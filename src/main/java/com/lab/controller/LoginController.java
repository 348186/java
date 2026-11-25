package com.lab.controller;

import com.lab.common.Result;
import com.lab.dto.LoginDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 最小登录接口：只写 session 标记，用于验证登录拦截器的放行/拦截效果。
 */
@RestController
public class LoginController {

    /** 根路径给出接口导航，避免直接访问应用时看到空白 404 */
    @GetMapping("/")
    public Result<Object> index() {
        return Result.ok(new java.util.LinkedHashMap<String, String>() {{
            put("login", "POST /login  {\"username\":\"admin\",\"password\":\"123456\"}");
            put("list", "GET /works?page=1&size=5  （需先登录拿 Cookie）");
            put("detail", "GET /works/{workId}");
            put("exception", "GET /test/business | /test/system | POST /test/validation");
            put("transaction", "POST /tx/without | /with | /catch | /self | /private");
        }});
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO, HttpSession session) {
        session.setAttribute("loginUser", loginDTO.getUsername());
        return Result.ok("登录成功");
    }

    @GetMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }
}
