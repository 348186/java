package com.lab.emp.common;

/**
 * 统一返回结果封装：code / message / data / timestamp。
 * 所有 RESTful 接口一律返回该结构，避免前端面对多种响应格式。
 */
public class Result<T> {

    private int code;

    private String message;

    private T data;

    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.OK.getCode();
        r.message = ResultCode.OK.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = ok(data);
        r.message = message;
        return r;
    }

    /** 失败返回：泛型化后可适配任意 data 类型的接口签名 */
    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.BIZ_ERROR, message);
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.message = message == null ? code.getMessage() : message;
        return r;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
