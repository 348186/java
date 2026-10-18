package com.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工持久层接口。
 * 继承 BaseMapper 后即拥有 MP 通用 CRUD；下列方法为手写 XML 动态 SQL，
 * 与 BaseMapper 共存（方法命名刻意避开 BaseMapper 已有签名，防止重载歧义）。
 */
public interface EmpMapper extends BaseMapper<Emp> {

    /** 查询全部员工 */
    List<Emp> findAll();

    /** 按主键查询 */
    Emp findByPrimaryKey(Integer empId);

    /** 多条件组合查询（if + where） */
    List<Emp> queryByExample(Emp condition);

    /** 分支查询：优先姓名精确 → 其次部门 → 否则全表（choose / when / otherwise） */
    List<Emp> queryByPriority(Emp condition);

    /** 新增员工，回填自增主键 */
    int addEmp(Emp emp);

    /** 批量新增 */
    int addInBatch(@Param("empList") List<Emp> empList);

    /** 全量更新 */
    int modifyEmp(Emp emp);

    /** 选择性更新（set + if） */
    int modifySelective(Emp emp);

    /** 按主键删除 */
    int removeEmp(Integer empId);

    /** 批量删除 */
    int removeInBatch(@Param("ids") List<Integer> ids);
}
