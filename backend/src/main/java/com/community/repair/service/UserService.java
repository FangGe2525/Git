package com.community.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.repair.dto.WorkerVO;
import com.community.repair.entity.Ticket;
import com.community.repair.entity.User;
import com.community.repair.enums.Role;
import com.community.repair.enums.TicketStatus;
import com.community.repair.mapper.TicketMapper;
import com.community.repair.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final TicketMapper ticketMapper;

    /**
     * 维修工列表 + 当前负载（已接单/处理中的工单数），派单时按负载排序。
     */
    public List<WorkerVO> listWorkers() {
        List<User> workers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Role.WORKER.name())
                .orderByAsc(User::getId));
        if (workers.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> load = new HashMap<>();
        List<Map<String, Object>> rows = ticketMapper.selectMaps(new QueryWrapper<Ticket>()
                .select("assigned_worker_id AS workerId", "COUNT(*) AS cnt")
                .in("status", TicketStatus.ACCEPTED.name(), TicketStatus.REPAIRING.name())
                .isNotNull("assigned_worker_id")
                .groupBy("assigned_worker_id"));
        for (Map<String, Object> row : rows) {
            Object id = row.get("workerId");
            Object cnt = row.get("cnt");
            if (id instanceof Number idNum && cnt instanceof Number cntNum) {
                load.put(idNum.longValue(), cntNum.longValue());
            }
        }

        return workers.stream()
                .map(w -> new WorkerVO(w.getId(), w.getUsername(), w.getRealName(), w.getPhone(),
                        w.getSkillTags(), load.getOrDefault(w.getId(), 0L)))
                .sorted((a, b) -> Long.compare(a.activeCount(), b.activeCount()))
                .toList();
    }
}
