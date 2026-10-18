package com.lab.emp.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.emp.common.Result;
import com.lab.emp.common.ResultCode;
import com.lab.emp.controller.AuthController;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器：未登录访问受保护资源时，直接返回 401 JSON，而不是重定向到登录页（前后端分离场景）。
 */
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthController.SESSION_KEY) != null) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage())));
        return false;
    }
}
