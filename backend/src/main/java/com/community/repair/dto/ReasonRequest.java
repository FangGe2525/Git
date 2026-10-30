package com.community.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 作废 / 返修 等需要说明原因的操作 */
@Data
public class ReasonRequest {

    @NotBlank(message = "不能为空")
    private String reason;
}
