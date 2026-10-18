package com.lab.emp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lab.emp.common.BusinessException;
import com.lab.emp.common.Result;
import com.lab.emp.common.ResultCode;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.service.EmpService;
import com.lab.entity.Emp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 员工 RESTful 接口。
 * 全部接口统一返回 Result，路径与方法遵循 REST 语义。
 */
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    /** 新增员工：POST /emps */
    @PostMapping
    public Result<Integer> add(@RequestBody @Valid Emp emp) {
        empService.save(emp);
        return Result.ok("新增成功", emp.getEmpId());
    }

    /** 修改员工：PUT /emps */
    @PutMapping
    public Result<Void> modify(@RequestBody @Valid Emp emp) {
        if (emp.getEmpId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "修改时必须携带 empId");
        }
        empService.updateById(emp);
        return Result.ok(null);
    }

    /** 单条删除：DELETE /emps/{id} */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Integer id) {
        Emp existed = empService.getById(id);
        if (existed == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在，id = " + id);
        }
        empService.removeById(id);
        return Result.ok(null);
    }

    /** 批量删除：DELETE /emps，请求体为 id 数组 */
    @DeleteMapping
    public Result<Integer> removeBatch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "批量删除的 id 集合不能为空");
        }
        int rows = empService.removeByIds(ids) ? ids.size() : 0;
        return Result.ok("批量删除成功", rows);
    }

    /** 单条查询：GET /emps/{id} */
    @GetMapping("/{id}")
    public Result<Emp> detail(@PathVariable Integer id) {
        Emp emp = empService.getById(id);
        if (emp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在，id = " + id);
        }
        return Result.ok(emp);
    }

    /** 分页 + 多条件查询：GET /emps?name=&gender=&dept=&minSalary=&status=&page=&size= */
    @GetMapping
    public Result<IPage<Emp>> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "5") long size,
                                   EmpQueryDTO query) {
        return Result.ok(empService.searchByPage(page, size, query));
    }

    /** 薪资排行：GET /emps/top?limit=3 */
    @GetMapping("/top")
    public Result<List<Emp>> topSalary(@RequestParam(defaultValue = "3") int limit) {
        return Result.ok(empService.findTopSalary(limit));
    }

    /** 事务回滚演示：POST /emps/tx-demo，观察数据库是否新增数据 */
    @PostMapping("/tx-demo")
    public Result<Void> txDemo(@RequestBody @Valid Emp emp) {
        empService.saveAndFail(emp);
        return Result.ok(null);
    }
}
