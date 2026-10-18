package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实验一 · MyBatis-Plus 单元测试（任务 4）。
 * BaseMapper 通用 CRUD、条件构造器、分页插件。
 * 断言基于「当前数据集合」动态推导，数据扩充后无需修改测试。
 */
class EmpMybatisPlusTest {

    @Test
    @DisplayName("步骤4.3：BaseMapper 通用 CRUD —— 查询与统计")
    void testBaseCrud() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            long total = mapper.selectCount(null);
            assertTrue(total >= 4, "emp 表至少应包含指导书给定的 4 条数据");
            System.out.println("当前员工总数 = " + total);

            Emp emp = mapper.selectById(1);
            assertNotNull(emp);
            assertEquals("张伟", emp.getEmpName());

            // 扩充数据同样可被查询到
            List<Emp> all = mapper.selectList(null);
            List<String> names = new ArrayList<>();
            all.forEach(e -> names.add(e.getEmpName()));
            assertTrue(names.containsAll(Arrays.asList("张三", "李四", "刘岩", "权小乐")));
        }
    }

    @Test
    @DisplayName("步骤4.3：BaseMapper 新增 / 修改 / 删除，随后还原")
    void testBaseWrite() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            long before = mapper.selectCount(null);

            Emp emp = new Emp("临时-MP", "男", "测试部", "临时岗",
                    new BigDecimal("8800"), LocalDate.of(2025, 3, 1), 1);

            assertEquals(1, mapper.insert(emp));
            session.commit();
            assertNotNull(emp.getEmpId());

            emp.setSalary(new BigDecimal("9900"));
            assertEquals(1, mapper.updateById(emp));
            session.commit();
            assertEquals(0, new BigDecimal("9900").compareTo(
                    mapper.selectById(emp.getEmpId()).getSalary()));

            assertEquals(1, mapper.deleteById(emp.getEmpId()));
            session.commit();
            assertEquals(before, mapper.selectCount(null), "删除后记录数应回到初始值");
        }
    }

    @Test
    @DisplayName("步骤4.4：QueryWrapper 多条件 + 排序")
    void testQueryWrapper() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            QueryWrapper<Emp> wrapper = new QueryWrapper<Emp>()
                    .eq("status", 1)
                    .ge("salary", new BigDecimal("9000"))
                    .orderByDesc("salary");
            List<Emp> list = mapper.selectList(wrapper);
            list.forEach(System.out::println);

            // 结果集必须与条件自洽，且按薪资倒序
            assertTrue(list.size() > 0);
            BigDecimal previous = null;
            for (Emp e : list) {
                assertEquals(1, e.getStatus());
                assertTrue(e.getSalary().compareTo(new BigDecimal("9000")) >= 0);
                if (previous != null) {
                    assertTrue(previous.compareTo(e.getSalary()) >= 0, "应按薪资倒序排列");
                }
                previous = e.getSalary();
            }
            assertEquals((long) list.size(), mapper.selectCount(wrapper));
        }
    }

    @Test
    @DisplayName("步骤4.4：LambdaQueryWrapper 写法，防止字段名写错")
    void testLambdaQueryWrapper() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                    .eq(Emp::getDept, "研发部")
                    .eq(Emp::getStatus, 1)
                    .orderByAsc(Emp::getEmpId);
            List<Emp> list = mapper.selectList(wrapper);
            assertTrue(list.size() >= 2);
            list.forEach(e -> {
                assertEquals("研发部", e.getDept());
                assertEquals(1, e.getStatus());
            });
        }
    }

    @Test
    @DisplayName("步骤4.5：分页插件 —— 第 1 页每页 3 条")
    void testPagination() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            long total = mapper.selectCount(null);

            IPage<Emp> page = mapper.selectPage(new Page<>(1, 3), null);
            System.out.println("总记录数 = " + page.getTotal());
            System.out.println("总页数   = " + page.getPages());
            page.getRecords().forEach(System.out::println);

            assertEquals(total, page.getTotal(), "total 应为全表记录数");
            assertEquals(3, page.getRecords().size(), "每页应为 3 条");
            assertEquals((total + 2) / 3, page.getPages(), "页数应按每页 3 条向上取整");
        }
    }

    @Test
    @DisplayName("步骤4.3：按 id 集合批量查询")
    void testSelectBatchIds() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            List<Emp> list = mapper.selectBatchIds(Arrays.asList(1, 2, 3));
            assertEquals(3, list.size());
        }
    }
}
