package com.lab.common;

/**
 * 可预期的业务异常，由 Service 层抛出，交给全局异常处理器转成统一响应。
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
