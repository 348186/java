package com.lab.emp.common;

/**
 * 业务异常：用于主动抛出可预期的业务错误（如查询的员工不存在、批量删除集合为空）。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ResultCode code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BIZ_ERROR;
    }

    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    public ResultCode getCode() {
        return code;
    }
}
