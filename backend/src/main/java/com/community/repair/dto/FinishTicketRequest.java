package com.community.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinishTicketRequest {

    @NotBlank(message = "不能为空")
    private String result;

    /** 有偿维修金额，为空或 0 表示无费用 */
    @PositiveOrZero(message = "不能为负数")
    private BigDecimal feeAmount;
}
