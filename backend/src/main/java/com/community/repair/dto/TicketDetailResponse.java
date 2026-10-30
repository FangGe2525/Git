package com.community.repair.dto;

import com.community.repair.entity.Ticket;
import com.community.repair.enums.TicketAction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单详情：工单本体 + 状态时间线。
 */
public record TicketDetailResponse(Ticket ticket, List<LogItem> logs) {

    public record LogItem(
            Long id,
            TicketAction action,
            String actionLabel,
            String fromStatus,
            String toStatus,
            Long operatorId,
            String operatorName,
            String remark,
            LocalDateTime createdAt) {
    }
}
