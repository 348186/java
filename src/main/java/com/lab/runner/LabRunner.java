package com.lab.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 实验一 · 步骤 2.4 第一个 MyBatis 程序。
 * 直接以 main 方法运行，观察控制台 SQL 日志与结果映射。
 */
public class LabRunner {

    public static void main(String[] args) {
        System.out.println("========== 1. 原生 MyBatis：查询全部员工 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            List<Emp> all = mapper.findAll();
            all.forEach(System.out::println);
            System.out.println("总人数 = " + all.size());
        }

        System.out.println("========== 2. 动态 SQL：多条件组合查询 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp condition = new Emp();
            condition.setDept("研发部");
            condition.setGender("女");
            mapper.queryByExample(condition).forEach(System.out::println);
        }

        System.out.println("========== 3. MyBatis-Plus：条件构造器 + 分页 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                    .ge(Emp::getSalary, new java.math.BigDecimal("8000"))
                    .orderByDesc(Emp::getSalary);
            Page<Emp> page = mapper.selectPage(new Page<>(1, 3), wrapper);
            System.out.println("总记录数 = " + page.getTotal());
            System.out.println("总页数   = " + page.getPages());
            page.getRecords().forEach(System.out::println);
        }
    }
}
