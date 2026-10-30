package com.community.repair.service;

import com.community.repair.common.BusinessException;
import com.community.repair.enums.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 工单状态机：集中定义合法流转，任何变更前都必须经过这里校验。
 * <p>转派、退回、返修、催办都是"动作"，只在必要时改变状态，本身不新增状态位。
 */
@Component
public class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED.put(TicketStatus.PENDING, EnumSet.of(TicketStatus.ACCEPTED, TicketStatus.CANCELED));
        // 已接单可退回（回到待接单，重新派给其他人）
        ALLOWED.put(TicketStatus.ACCEPTED, EnumSet.of(TicketStatus.REPAIRING, TicketStatus.PENDING, TicketStatus.CANCELED));
        // 处理中可退回（无法维修）
        ALLOWED.put(TicketStatus.REPAIRING, EnumSet.of(TicketStatus.VERIFYING, TicketStatus.PENDING, TicketStatus.CANCELED));
        // 待确认可确认通过，也可打回返修
        ALLOWED.put(TicketStatus.VERIFYING, EnumSet.of(TicketStatus.COMPLETED, TicketStatus.REPAIRING, TicketStatus.CANCELED));
        ALLOWED.put(TicketStatus.COMPLETED, EnumSet.noneOf(TicketStatus.class));
        ALLOWED.put(TicketStatus.CANCELED, EnumSet.noneOf(TicketStatus.class));
    }

    public boolean canTransit(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public void assertCanTransit(TicketStatus from, TicketStatus to) {
        if (!canTransit(from, to)) {
            throw new BusinessException(
                    "工单状态不允许从「" + (from == null ? "-" : from.getLabel()) + "」变更为「" + to.getLabel() + "」");
        }
    }

    public Set<TicketStatus> nextOf(TicketStatus from) {
        return ALLOWED.getOrDefault(from, Set.of());
    }
}
