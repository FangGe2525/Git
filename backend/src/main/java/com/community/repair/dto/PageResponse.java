package com.community.repair.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/** 分页响应 */
public record PageResponse<T>(long total, long pages, long current, long size, List<T> records) {

    public static <T> PageResponse<T> of(IPage<T> page) {
        return new PageResponse<>(page.getTotal(), page.getPages(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
