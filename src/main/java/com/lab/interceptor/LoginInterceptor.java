package com.lab.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：检查 session 中的 loginUser，未登录返回 401 与结构化 JSON。
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 跨域预检请求不携带会话，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object loginUser = session == null ? null : session.getAttribute("loginUser");
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":401,\"message\":\"未登录，请先登录\",\"data\":null}");
            return false;
        }
        return true;
    }
}
