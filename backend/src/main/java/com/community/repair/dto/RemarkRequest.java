package com.community.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RemarkRequest {

    @NotBlank(message = "不能为空")
    private String content;
}
