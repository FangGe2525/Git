package com.community.repair.common;

import lombok.Getter;

/**
 * 业务异常。由 Service 层在业务规则不满足时抛出，统一转成失败响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }
}
