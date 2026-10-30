package com.community.repair.common;

import lombok.Getter;

/**
 * 业务返回码。0 表示成功，其余为失败。
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(400, "请求参数不合法"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    BUSINESS_ERROR(1000, "业务处理失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
