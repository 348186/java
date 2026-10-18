package com.lab.emp.common;

/**
 * 统一返回码。使用枚举代替魔法数字，便于前后端对齐与后期维护。
 */
public enum ResultCode {

    /** 成功 */
    OK(200, "success"),
    /** 参数校验失败 */
    BAD_REQUEST(400, "参数不合法"),
    /** 未登录或登录已失效 */
    UNAUTHORIZED(401, "未登录或登录已失效"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 业务异常 */
    BIZ_ERROR(500, "业务处理失败"),
    /** 系统异常 */
    ERROR(500, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
