package com.community.repair.controller;

import com.community.repair.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查：进程存活即返回 status=ok，同时附带数据库连通性，方便区分“服务未起”与“库连不上”。
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        try {
            jdbcTemplate.queryForObject("select 1", Integer.class);
            body.put("db", "up");
        } catch (Exception ex) {
            body.put("db", "down");
            body.put("dbError", truncate(ex.getMessage()));
            log.warn("数据库连接检查失败: {}", ex.getMessage());
        }
        return ApiResponse.ok(body);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 160 ? message : message.substring(0, 160) + "...";
    }
}
