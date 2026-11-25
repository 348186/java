package com.lab.aspect;

import com.lab.dto.LoginDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Controller 层请求日志切面：记录方法、参数摘要、耗时与异常。
 */
@Aspect
@Component
public class WebLogAspect {

    @Pointcut("execution(* com.lab.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        System.out.println("[REQ] " + method + " 参数：" + safeArgs(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            System.out.println("[RESP] " + method + " 耗时："
                    + (System.currentTimeMillis() - start) + "ms");
            return result;
        } catch (Throwable t) {
            System.err.println("[ERR] " + method + " 异常：" + t.getMessage());
            throw t;
        }
    }

    /** 登录密码不写入日志，避免敏感信息泄露 */
    private String safeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> arg instanceof LoginDTO ? "LoginDTO(密码已脱敏)" : arg)
                .toList()
                .toString();
    }
}
