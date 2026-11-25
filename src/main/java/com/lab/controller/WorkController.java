package com.lab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.common.Result;
import com.lab.dto.WorkQueryDTO;
import com.lab.dto.WorkSaveDTO;
import com.lab.entity.PhotoWorkPlus;
import com.lab.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 摄影作品 RESTful 接口：POST 新增、GET 查询、PUT 修改、DELETE 删除。
 */
@RestController
@RequestMapping("/works")
public class WorkController {

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody WorkSaveDTO dto) {
        workService.save(toEntity(dto));
        return Result.ok();
    }

    @PutMapping("/{workId}")
    public Result<Void> update(@PathVariable("workId") Integer workId,
                               @Valid @RequestBody WorkSaveDTO dto) {
        PhotoWorkPlus work = toEntity(dto);
        work.setWorkId(workId);
        workService.updateById(work);
        return Result.ok();
    }

    @DeleteMapping("/{workId}")
    public Result<Void> delete(@PathVariable("workId") Integer workId) {
        workService.removeById(workId);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        workService.removeByIds(ids);
        return Result.ok();
    }

    @GetMapping("/{workId}")
    public Result<PhotoWorkPlus> getById(@PathVariable("workId") Integer workId) {
        return Result.ok(workService.getById(workId));
    }

    /** 分页 + 多条件查询，条件对象由 Spring 按参数名自动绑定 */
    @GetMapping
    public Result<Page<PhotoWorkPlus>> page(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            WorkQueryDTO query) {
        return Result.ok(workService.pageQuery(page, size, query));
    }

    private PhotoWorkPlus toEntity(WorkSaveDTO dto) {
        return new PhotoWorkPlus(dto.getPhotographerId(), dto.getWorkTitle(), dto.getLocation(),
                dto.getShootDate(), dto.getStyle(), dto.getPrice(), dto.getStatus());
    }
}
