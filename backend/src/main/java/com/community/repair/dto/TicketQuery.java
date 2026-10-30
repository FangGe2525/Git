package com.community.repair.dto;

import com.community.repair.enums.TicketSource;
import com.community.repair.enums.TicketStatus;
import lombok.Data;

/**
 * 工单列表查询条件。
 */
@Data
public class TicketQuery {

    private long page = 1;

    private long size = 10;

    private TicketStatus status;

    private String building;

    private String category;

    private Long workerId;

    private TicketSource source;

    /** 单号 / 描述 / 位置 模糊匹配 */
    private String keyword;
}
