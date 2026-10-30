package com.community.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.community.repair.enums.FeeStatus;
import com.community.repair.enums.TicketPriority;
import com.community.repair.enums.TicketSource;
import com.community.repair.enums.TicketStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "tickets", autoResultMap = true)
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private TicketSource source;

    private Long ownerId;

    private String reporterName;

    private String reporterPhone;

    private String building;

    private String unit;

    private String room;

    /** 二维码点位编码，如 B3-1201 */
    private String locationCode;

    private String locationDetail;

    /** 工种 */
    private String category;

    /** 问题分类 */
    private String problemType;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> mediaUrls;

    private TicketStatus status;

    private TicketPriority priority;

    private Long assignedWorkerId;

    private LocalDateTime acceptedAt;

    private LocalDateTime arrivedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime confirmedAt;

    private FeeStatus feeStatus;

    private BigDecimal feeAmount;

    private Integer reworkCount;

    private Long parentTicketId;

    private String cancelReason;

    private Long cancelBy;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
