package com.lab.controller;

import com.lab.common.Result;
import com.lab.entity.PhotoWorkPlus;
import com.lab.service.WorkService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事务场景入口：每次请求用不同标题写入测试数据，再通过 /tx/count 判断是否回滚。
 */
@RestController
@RequestMapping("/tx")
public class TxController {

    private final WorkService workService;

    public TxController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping("/without")
    public Result<Void> withoutTx(@RequestBody PhotoWorkPlus work) {
        workService.saveWithoutTx(work);
        return Result.ok();
    }

    @PostMapping("/with")
    public Result<Void> withTx(@RequestBody PhotoWorkPlus work) {
        workService.saveWithTx(work);
        return Result.ok();
    }

    @PostMapping("/catch")
    public Result<Void> catchException(@RequestBody PhotoWorkPlus work) {
        workService.saveWithTxCatchException(work);
        return Result.ok();
    }

    @PostMapping("/self")
    public Result<Void> selfInvoke(@RequestBody PhotoWorkPlus work) {
        workService.saveWithTxSelfInvoke(work);
        return Result.ok();
    }

    @PostMapping("/private")
    public Result<Void> privateMethod(@RequestBody PhotoWorkPlus work) {
        workService.saveWithTxPrivate(work);
        return Result.ok();
    }

    @GetMapping("/count")
    public Result<Long> count(@RequestParam(name = "title") String title) {
        return Result.ok(workService.countByTitle(title));
    }

    @DeleteMapping("/clean")
    public Result<Integer> clean(@RequestParam(name = "title") String title) {
        return Result.ok(workService.deleteByTitle(title));
    }
}
