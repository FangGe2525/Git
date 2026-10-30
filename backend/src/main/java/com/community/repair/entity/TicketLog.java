package com.community.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.repair.enums.TicketAction;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单流转日志。状态变更与关键动作（派单、催办、备注）都写这里。
 */
@Data
@TableName("ticket_logs")
public class TicketLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private TicketAction action;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String remark;

    private LocalDateTime createdAt;
}
