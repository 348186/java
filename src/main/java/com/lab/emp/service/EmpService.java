package com.lab.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.entity.Emp;

import java.util.List;

/**
 * 员工业务接口。继承 MP 的 IService 获得通用 CRUD，另自定义分页与批量方法。
 */
public interface EmpService extends IService<Emp> {

    /**
     * 分页 + 多条件查询
     *
     * @param current 当前页（从 1 开始）
     * @param size    每页条数
     * @param query   查询条件
     */
    IPage<Emp> searchByPage(long current, long size, EmpQueryDTO query);

    /** 在职员工薪资统计（演示自定义聚合业务方法） */
    List<Emp> findTopSalary(int limit);

    /** 声明式事务演示：新增后抛异常，验证回滚 */
    void saveAndFail(Emp emp);
}
