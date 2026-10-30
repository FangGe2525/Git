package com.community.repair.dto;

import com.community.repair.enums.TicketPriority;
import com.community.repair.enums.TicketSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 建单请求。业主端只传位置与描述；客服代报修时可指定来源、报修人和业主。
 */
@Data
public class CreateTicketRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 2000, message = "不能超过 2000 字")
    private String description;

    private String building;

    private String unit;

    private String room;

    /** 二维码点位编码 */
    private String locationCode;

    private String locationDetail;

    /** 工种 */
    private String category;

    /** 问题分类 */
    private String problemType;

    private TicketPriority priority;

    /** 仅客服代报修时使用 */
    private TicketSource source;

    private Long ownerId;

    private String reporterName;

    private String reporterPhone;

    private List<String> mediaUrls;
}
