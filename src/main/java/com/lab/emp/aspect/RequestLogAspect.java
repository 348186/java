package com.lab.emp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 请求日志切面：记录 URI、参数、耗时与异常，便于接口排障。
 * 与拦截器的区别：拦截器工作在 DispatcherServlet 之内（可阻断请求），
 * 切面工作在方法调用层面（可作用到任意 Bean 的方法）。
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    @Pointcut("execution(* com.lab.emp.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long begin = System.currentTimeMillis();
        String uri = currentUri();
        String method = joinPoint.getSignature().toShortString();
        log.info("[REQ] {} args={}", uri + " -> " + method, Arrays.toString(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            log.info("[RESP] {} cost={}ms", method, System.currentTimeMillis() - begin);
            return result;
        } catch (Throwable t) {
            log.error("[ERR] {} cost={}ms msg={}", method,
                    System.currentTimeMillis() - begin, t.getMessage());
            throw t;
        }
    }

    private String currentUri() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "-";
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getMethod() + " " + request.getRequestURI();
    }
}
