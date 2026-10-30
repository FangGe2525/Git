package com.community.repair.dto;

/** 派单时展示的维修工，含当前在手工单数 */
public record WorkerVO(
        Long id,
        String username,
        String realName,
        String phone,
        String skillTags,
        long activeCount) {
}
