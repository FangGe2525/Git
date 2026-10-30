package com.community.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.repair.common.BusinessException;
import com.community.repair.common.ErrorCode;
import com.community.repair.common.SecurityUtils;
import com.community.repair.dto.AssignTicketRequest;
import com.community.repair.dto.CreateTicketRequest;
import com.community.repair.dto.FinishTicketRequest;
import com.community.repair.dto.PageResponse;
import com.community.repair.dto.TicketDetailResponse;
import com.community.repair.dto.TicketQuery;
import com.community.repair.entity.Ticket;
import com.community.repair.entity.TicketLog;
import com.community.repair.entity.User;
import com.community.repair.enums.FeeStatus;
import com.community.repair.enums.Role;
import com.community.repair.enums.TicketAction;
import com.community.repair.enums.TicketPriority;
import com.community.repair.enums.TicketSource;
import com.community.repair.enums.TicketStatus;
import com.community.repair.mapper.TicketLogMapper;
import com.community.repair.mapper.TicketMapper;
import com.community.repair.mapper.UserMapper;
import com.community.repair.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_PAGE_SIZE = 100;

    private final TicketMapper ticketMapper;
    private final TicketLogMapper ticketLogMapper;
    private final UserMapper userMapper;
    private final TicketStateMachine stateMachine;

    // ---------------------------------------------------------------- 建单

    @Transactional
    public Ticket create(CreateTicketRequest request, LoginUser me) {
        boolean isOwner = Role.OWNER.name().equals(me.getRole());
        if (!isOwner && !SecurityUtils.isStaff(me)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有业主、客服或社区工作人员可以建单");
        }

        Ticket ticket = new Ticket();
        ticket.setSource(isOwner
                ? TicketSource.SCAN
                : (request.getSource() == null ? TicketSource.PHONE : request.getSource()));
        ticket.setOwnerId(isOwner ? me.getId() : request.getOwnerId());
        ticket.setCreatedBy(isOwner ? null : me.getId());
        ticket.setReporterName(StringUtils.hasText(request.getReporterName())
                ? request.getReporterName()
                : (isOwner ? me.getRealName() : null));
        ticket.setReporterPhone(request.getReporterPhone());
        ticket.setBuilding(request.getBuilding());
        ticket.setUnit(request.getUnit());
        ticket.setRoom(request.getRoom());
        ticket.setLocationCode(request.getLocationCode());
        ticket.setLocationDetail(request.getLocationDetail());
        ticket.setCategory(request.getCategory());
        ticket.setProblemType(request.getProblemType());
        ticket.setDescription(request.getDescription());
        ticket.setMediaUrls(request.getMediaUrls());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setPriority(request.getPriority() == null ? TicketPriority.MEDIUM : request.getPriority());
        ticket.setFeeStatus(FeeStatus.NONE);
        ticket.setReworkCount(0);

        insertWithUniqueNo(ticket);
        writeLog(ticket.getId(), TicketAction.CREATE, null, TicketStatus.PENDING, me.getId(), "创建工单");
        return ticket;
    }

    /** 单号 WX + 日期 + 4 位当日序号；并发下靠唯一索引兜底重试 */
    private void insertWithUniqueNo(Ticket ticket) {
        for (int attempt = 1; attempt <= 5; attempt++) {
            ticket.setTicketNo(nextTicketNo());
            try {
                ticketMapper.insert(ticket);
                return;
            } catch (DuplicateKeyException ex) {
                log.warn("单号冲突，重试第 {} 次: {}", attempt, ticket.getTicketNo());
                ticket.setId(null);
            }
        }
        throw new BusinessException("工单号生成失败，请稍后重试");
    }

    private String nextTicketNo() {
        LocalDate today = LocalDate.now();
        Long count = ticketMapper.selectCount(new LambdaQueryWrapper<Ticket>()
                .ge(Ticket::getCreatedAt, today.atStartOfDay()));
        return "WX" + today.format(DAY) + String.format("%04d", (count == null ? 0 : count) + 1);
    }

    // ---------------------------------------------------------------- 查询

    public PageResponse<Ticket> list(TicketQuery query, LoginUser me) {
        long size = query.getSize() <= 0 ? 10 : Math.min(query.getSize(), MAX_PAGE_SIZE);
        Page<Ticket> page = new Page<>(Math.max(1, query.getPage()), size);

        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        applyVisibility(wrapper, me);
        if (query.getStatus() != null) {
            wrapper.eq(Ticket::getStatus, query.getStatus().name());
        }
        if (StringUtils.hasText(query.getBuilding())) {
            wrapper.eq(Ticket::getBuilding, query.getBuilding());
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Ticket::getCategory, query.getCategory());
        }
        if (query.getWorkerId() != null) {
            wrapper.eq(Ticket::getAssignedWorkerId, query.getWorkerId());
        }
        if (query.getSource() != null) {
            wrapper.eq(Ticket::getSource, query.getSource().name());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Ticket::getTicketNo, keyword)
                    .or().like(Ticket::getDescription, keyword)
                    .or().like(Ticket::getLocationDetail, keyword));
        }
        wrapper.orderByDesc(Ticket::getCreatedAt);

        return PageResponse.of(ticketMapper.selectPage(page, wrapper));
    }

    public TicketDetailResponse detail(Long id, LoginUser me) {
        Ticket ticket = requireTicket(id);
        assertCanView(ticket, me);
        return new TicketDetailResponse(ticket, timeline(id));
    }

    public List<TicketDetailResponse.LogItem> timeline(Long ticketId) {
        List<TicketLog> logs = ticketLogMapper.selectList(new LambdaQueryWrapper<TicketLog>()
                .eq(TicketLog::getTicketId, ticketId)
                .orderByAsc(TicketLog::getCreatedAt)
                .orderByAsc(TicketLog::getId));
        if (logs.isEmpty()) {
            return List.of();
        }
        Set<Long> operatorIds = logs.stream()
                .map(TicketLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> names = operatorIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getRealName() == null ? u.getUsername() : u.getRealName()));

        return logs.stream()
                .map(l -> new TicketDetailResponse.LogItem(
                        l.getId(),
                        l.getAction(),
                        l.getAction() == null ? null : l.getAction().getLabel(),
                        l.getFromStatus(),
                        l.getToStatus(),
                        l.getOperatorId(),
                        l.getOperatorId() == null ? null : names.getOrDefault(l.getOperatorId(), "系统"),
                        l.getRemark(),
                        l.getCreatedAt()))
                .toList();
    }

    // ---------------------------------------------------------------- 状态流转

    @Transactional
    public Ticket assign(Long id, AssignTicketRequest request, LoginUser me) {
        assertStaff(me, "只有客服或社区工作人员可以派单");
        Ticket ticket = requireTicket(id);
        if (ticket.getStatus().isTerminal()) {
            throw new BusinessException("工单已" + ticket.getStatus().getLabel() + "，无法派单");
        }
        User worker = userMapper.selectById(request.getWorkerId());
        if (worker == null || !Role.WORKER.name().equals(worker.getRole())) {
            throw new BusinessException("指定的用户不是维修工");
        }

        TicketStatus from = ticket.getStatus();
        boolean reassign = ticket.getAssignedWorkerId() != null;
        if (from == TicketStatus.ACCEPTED) {
            // 已接单后转派：回到待接单，由新维修工重新接单
            stateMachine.assertCanTransit(from, TicketStatus.PENDING);
            ticket.setStatus(TicketStatus.PENDING);
            ticket.setAcceptedAt(null);
        }
        ticket.setAssignedWorkerId(worker.getId());
        ticketMapper.updateById(ticket);

        String remark = StringUtils.hasText(request.getRemark())
                ? request.getRemark()
                : ("指派给 " + displayName(worker));
        writeLog(ticket.getId(), reassign ? TicketAction.REASSIGN : TicketAction.ASSIGN,
                from, ticket.getStatus(), me.getId(), remark);
        return ticket;
    }

    @Transactional
    public Ticket accept(Long id, LoginUser me) {
        Ticket ticket = requireTicket(id);
        requireAssignedWorker(ticket, me);
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.ACCEPTED);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.ACCEPTED);
        ticket.setAcceptedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.ACCEPT, from, ticket.getStatus(), me.getId(), "维修工接单");
        return ticket;
    }

    @Transactional
    public Ticket start(Long id, LoginUser me) {
        Ticket ticket = requireTicket(id);
        requireAssignedWorker(ticket, me);
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.REPAIRING);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.REPAIRING);
        ticket.setArrivedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.START, from, ticket.getStatus(), me.getId(), "到场，开始处理");
        return ticket;
    }

    @Transactional
    public Ticket finish(Long id, FinishTicketRequest request, LoginUser me) {
        Ticket ticket = requireTicket(id);
        requireAssignedWorker(ticket, me);
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.VERIFYING);

        // 完工必须有现场照片作为凭证，业主据此确认
        List<String> photos = request.getMediaUrls();
        if (photos == null || photos.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请至少上传一张完工照片");
        }
        for (String url : photos) {
            if (url == null || !url.startsWith("/uploads/")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "完工照片地址不合法");
            }
        }

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.VERIFYING);
        ticket.setFinishedAt(LocalDateTime.now());
        ticket.setFinishMediaUrls(photos);

        BigDecimal fee = request.getFeeAmount();
        if (fee != null && fee.compareTo(BigDecimal.ZERO) > 0) {
            ticket.setFeeAmount(fee);
            ticket.setFeeStatus(FeeStatus.PENDING);
        } else {
            ticket.setFeeStatus(FeeStatus.NONE);
        }
        ticketMapper.updateById(ticket);

        String remark = request.getResult() + "（附 " + photos.size() + " 张完工照片）";
        writeLog(ticket.getId(), TicketAction.FINISH, from, ticket.getStatus(), me.getId(), remark);
        return ticket;
    }

    @Transactional
    public Ticket confirm(Long id, LoginUser me) {
        Ticket ticket = requireTicket(id);
        boolean isOwner = Objects.equals(ticket.getOwnerId(), me.getId());
        if (!isOwner && !SecurityUtils.isStaff(me)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有报修业主或客服可以确认完工");
        }
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.COMPLETED);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setConfirmedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.CONFIRM, from, ticket.getStatus(), me.getId(),
                isOwner ? "业主确认完工" : "客服代确认完工");
        return ticket;
    }

    @Transactional
    public Ticket rework(Long id, String reason, LoginUser me) {
        Ticket ticket = requireTicket(id);
        boolean isOwner = Objects.equals(ticket.getOwnerId(), me.getId());
        if (!isOwner && !SecurityUtils.isStaff(me)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有报修业主或客服可以发起返修");
        }
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.REPAIRING);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.REPAIRING);
        ticket.setReworkCount((ticket.getReworkCount() == null ? 0 : ticket.getReworkCount()) + 1);
        ticket.setFinishedAt(null);
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.REWORK, from, ticket.getStatus(), me.getId(), reason);
        return ticket;
    }

    @Transactional
    public Ticket returnTicket(Long id, String reason, LoginUser me) {
        Ticket ticket = requireTicket(id);
        requireAssignedWorker(ticket, me);
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.PENDING);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setAssignedWorkerId(null);
        ticket.setAcceptedAt(null);
        ticket.setArrivedAt(null);
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.RETURN, from, ticket.getStatus(), me.getId(), reason);
        return ticket;
    }

    @Transactional
    public Ticket cancel(Long id, String reason, LoginUser me) {
        assertStaff(me, "只有客服或社区工作人员可以作废工单");
        Ticket ticket = requireTicket(id);
        stateMachine.assertCanTransit(ticket.getStatus(), TicketStatus.CANCELED);

        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELED);
        ticket.setCancelReason(reason);
        ticket.setCancelBy(me.getId());
        ticketMapper.updateById(ticket);
        writeLog(ticket.getId(), TicketAction.CANCEL, from, ticket.getStatus(), me.getId(), reason);
        return ticket;
    }

    @Transactional
    public void remark(Long id, String content, LoginUser me) {
        Ticket ticket = requireTicket(id);
        assertCanView(ticket, me);
        writeLog(ticket.getId(), TicketAction.REMARK, ticket.getStatus(), ticket.getStatus(), me.getId(), content);
    }

    @Transactional
    public void urge(Long id, LoginUser me) {
        Ticket ticket = requireTicket(id);
        assertCanView(ticket, me);
        if (ticket.getStatus().isTerminal()) {
            throw new BusinessException("工单已结束，无需催办");
        }
        writeLog(ticket.getId(), TicketAction.URGE, ticket.getStatus(), ticket.getStatus(), me.getId(), "催办");
    }

    // ---------------------------------------------------------------- 内部方法

    private Ticket requireTicket(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        return ticket;
    }

    private void assertStaff(LoginUser me, String message) {
        if (!SecurityUtils.isStaff(me)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, message);
        }
    }

    private void requireAssignedWorker(Ticket ticket, LoginUser me) {
        if (!Role.WORKER.name().equals(me.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有维修工可以执行该操作");
        }
        if (!Objects.equals(ticket.getAssignedWorkerId(), me.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该工单未指派给你");
        }
    }

    /** 数据可见范围：业主看自己的，维修工看派给自己的，客服/社区看全部 */
    private void assertCanView(Ticket ticket, LoginUser me) {
        if (SecurityUtils.isStaff(me)) {
            return;
        }
        if (Role.OWNER.name().equals(me.getRole()) && Objects.equals(ticket.getOwnerId(), me.getId())) {
            return;
        }
        if (Role.WORKER.name().equals(me.getRole()) && Objects.equals(ticket.getAssignedWorkerId(), me.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该工单");
    }

    private void applyVisibility(LambdaQueryWrapper<Ticket> wrapper, LoginUser me) {
        if (Role.OWNER.name().equals(me.getRole())) {
            wrapper.eq(Ticket::getOwnerId, me.getId());
        } else if (Role.WORKER.name().equals(me.getRole())) {
            wrapper.eq(Ticket::getAssignedWorkerId, me.getId());
        }
    }

    private String displayName(User user) {
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
    }

    private void writeLog(Long ticketId, TicketAction action, TicketStatus from, TicketStatus to,
                          Long operatorId, String remark) {
        TicketLog log = new TicketLog();
        log.setTicketId(ticketId);
        log.setAction(action);
        log.setFromStatus(from == null ? null : from.name());
        log.setToStatus(to == null ? null : to.name());
        log.setOperatorId(operatorId);
        log.setRemark(remark != null && remark.length() > 500 ? remark.substring(0, 500) : remark);
        ticketLogMapper.insert(log);
    }
}
