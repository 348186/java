package com.lab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 最小登录请求：仅用于验证登录拦截器，不接入真实账号体系。
 */
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
