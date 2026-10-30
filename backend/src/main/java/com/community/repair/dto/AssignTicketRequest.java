package com.community.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTicketRequest {

    @NotNull(message = "不能为空")
    private Long workerId;

    private String remark;
}
