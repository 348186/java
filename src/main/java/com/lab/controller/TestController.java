package com.lab.controller;

import com.lab.common.BusinessException;
import com.lab.common.Result;
import com.lab.dto.WorkSaveDTO;
import com.lab.service.VerifyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时验证接口：被登录拦截器排除，便于单独验证数据库连通与全局异常处理器。
 */
@RestController
@RequestMapping("/test")
public class TestController {

    private final VerifyService verifyService;

    public TestController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    /** 步骤 3.4：整合验证，确认数据源、Mapper 注入与字段映射均正常 */
    @GetMapping("/db")
    public Result<String> db() {
        return Result.ok(verifyService.verifyDb());
    }

    @PostMapping("/validation")
    public Result<Void> validation(@Valid @RequestBody WorkSaveDTO dto) {
        return Result.ok();
    }

    @GetMapping("/business")
    public Result<Void> business() {
        throw new BusinessException(400, "作品不存在");
    }

    @GetMapping("/system")
    public Result<Void> system() {
        throw new RuntimeException("模拟系统异常：数据库连接失败");
    }
}
