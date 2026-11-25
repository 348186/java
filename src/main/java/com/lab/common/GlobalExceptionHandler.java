package com.lab.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常出口：Controller 不再重复 try-catch。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** JSR-303 参数校验失败：返回第一个字段提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(400, message);
    }

    /** 方法级校验异常（如 @Validated 触发） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(400, message);
    }

    /** 业务异常：保留业务 code 与提示 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 未知系统异常：内部记录日志，外部隐藏堆栈细节 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        System.err.println("[SYS-ERR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        return Result.fail(500, "系统异常，请联系管理员");
    }
}
