package com.lab.emp.controller;

import com.lab.emp.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 极简登录接口，仅为配合登录拦截器演示（教学用，不做真实鉴权）。
 * 调用顺序：POST /auth/login 建立会话 → 再访问 /emps/**。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /** 会话中存放登录用户使用的 key */
    public static final String SESSION_KEY = "LOGIN_USER";

    /** 登录：POST /auth/login，body 形如 {"username":"admin"} */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body,
                                             HttpServletRequest request) {
        String username = body == null ? null : body.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_KEY, username.trim());

        Map<String, Object> data = new HashMap<>();
        data.put("username", username.trim());
        data.put("sessionId", session.getId());
        return Result.ok("登录成功", data);
    }

    /** 校验登录态：GET /auth/check */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(HttpServletRequest request) {
        Object user = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute(SESSION_KEY);
        Map<String, Object> data = new HashMap<>();
        data.put("login", user != null);
        data.put("username", user);
        return Result.ok(data);
    }

    /** 退出：POST /auth/logout */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.ok(null);
    }
}
