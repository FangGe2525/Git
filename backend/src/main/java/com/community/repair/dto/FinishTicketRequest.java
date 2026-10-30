package com.community.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinishTicketRequest {

    @NotBlank(message = "不能为空")
    private String result;

    /** 完工照片，必须至少一张，由业主确认时查看 */
    @NotEmpty(message = "请至少上传一张完工照片")
    @Size(max = 6, message = "最多 6 张")
    private List<String> mediaUrls;

    /** 有偿维修金额，为空或 0 表示无费用 */
    @PositiveOrZero(message = "不能为负数")
    private BigDecimal feeAmount;
}
