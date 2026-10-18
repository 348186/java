package com.lab;

import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实验一 · 原生 MyBatis 单元测试（任务 2、任务 3）。
 *
 * <p>设计约定：所有写操作在测试结束时自行还原，保证 emp 表数据始终是
 * 「指导书 4 条 + 扩充 10 条」的固定集合。
 * 断言尽量不写死行数，而是与 MP 的 selectCount() 交叉校验，
 * 这样后续再增删数据时测试不会失效。
 */
class EmpMapperXmlTest {

    private Emp buildTempEmp(String name) {
        return new Emp(name, "女", "测试部", "临时岗",
                new BigDecimal("9000"), LocalDate.of(2025, 1, 1), 1);
    }

    @Test
    @DisplayName("步骤2.4：查询全部员工，验证指导书 4 条数据均存在")
    void testFindAll() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            List<Emp> list = mapper.findAll();
            list.forEach(System.out::println);

            // 与 MP 通用方法交叉校验，避免把行数写死
            assertEquals((long) mapper.selectCount(null), (long) list.size());

            // 指导书给定的 4 条数据必须存在
            List<String> names = new ArrayList<>();
            list.forEach(e -> names.add(e.getEmpName()));
            assertTrue(names.containsAll(List.of("张伟", "李娜", "王强", "赵敏")));
        }
    }

    @Test
    @DisplayName("步骤2.3：按主键查询，验证结果映射")
    void testFindByPrimaryKey() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            Emp emp = session.getMapper(EmpMapper.class).findByPrimaryKey(1);
            assertNotNull(emp);
            assertEquals("张伟", emp.getEmpName());
            assertEquals("研发部", emp.getDept());
            assertEquals(LocalDate.of(2024, 7, 1), emp.getHireDate());
        }
    }

    @Test
    @DisplayName("步骤2.3：新增并回填自增主键，随后清理")
    void testAddEmp() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp emp = buildTempEmp("临时-新增");
            int rows = mapper.addEmp(emp);
            session.commit();
            assertEquals(1, rows);
            assertNotNull(emp.getEmpId(), "useGeneratedKeys 应回填主键");
            System.out.println("回填主键 empId = " + emp.getEmpId());

            // 还原数据
            mapper.removeEmp(emp.getEmpId());
            session.commit();
            assertNull(mapper.findByPrimaryKey(emp.getEmpId()));
        }
    }

    @Test
    @DisplayName("步骤2.3：修改员工薪资，随后还原")
    void testModifyEmp() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp emp = mapper.findByPrimaryKey(2);
            BigDecimal origin = emp.getSalary();

            emp.setSalary(new BigDecimal("13500"));
            assertEquals(1, mapper.modifyEmp(emp));
            session.commit();
            assertEquals(0, new BigDecimal("13500").compareTo(
                    mapper.findByPrimaryKey(2).getSalary()));

            // 还原数据
            emp.setSalary(origin);
            mapper.modifyEmp(emp);
            session.commit();
            assertEquals(0, origin.compareTo(mapper.findByPrimaryKey(2).getSalary()));
        }
    }

    @Test
    @DisplayName("步骤3.1：多条件组合查询（if + where）")
    void testQueryByExample() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            // 单条件：部门
            Emp byDept = new Emp();
            byDept.setDept("研发部");
            List<Emp> deptList = mapper.queryByExample(byDept);
            assertTrue(deptList.size() >= 2);
            deptList.forEach(e -> assertEquals("研发部", e.getDept()));
            assertEquals((long) deptList.size(), mapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Emp>()
                            .eq(Emp::getDept, "研发部")));

            // 双条件：部门 + 性别
            Emp byDeptGender = new Emp();
            byDeptGender.setDept("研发部");
            byDeptGender.setGender("女");
            List<Emp> list = mapper.queryByExample(byDeptGender);
            list.forEach(e -> {
                assertEquals("研发部", e.getDept());
                assertEquals("女", e.getGender());
            });
            assertTrue(list.stream().anyMatch(e -> "李娜".equals(e.getEmpName())));

            // 薪资下限
            Emp bySalary = new Emp();
            bySalary.setSalary(new BigDecimal("12000"));
            mapper.queryByExample(bySalary).forEach(
                    e -> assertTrue(e.getSalary().compareTo(new BigDecimal("12000")) >= 0));

            // 无条件时 <where> 不生成 WHERE 子句，等价于全表查询
            assertEquals((long) mapper.selectCount(null),
                    (long) mapper.queryByExample(new Emp()).size());
        }
    }

    @Test
    @DisplayName("步骤3.2：动态更新（set + if）只更新非空字段")
    void testModifySelective() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp emp = mapper.findByPrimaryKey(3);
            String originDept = emp.getDept();

            Emp patch = new Emp();
            patch.setEmpId(3);
            patch.setDept("市场二部");
            assertEquals(1, mapper.modifySelective(patch));
            session.commit();
            assertEquals("市场二部", mapper.findByPrimaryKey(3).getDept());
            // post 为 null，不应被更新
            assertEquals(emp.getPost(), mapper.findByPrimaryKey(3).getPost());

            // 还原数据
            patch.setDept(originDept);
            mapper.modifySelective(patch);
            session.commit();
            assertEquals(originDept, mapper.findByPrimaryKey(3).getDept());
        }
    }

    @Test
    @DisplayName("步骤3.3：批量新增与批量删除（foreach）")
    void testBatch() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            List<Emp> batch = new ArrayList<>();
            batch.add(new Emp("批量-甲", "女", "批量测试部", "临时岗",
                    new BigDecimal("9000"), LocalDate.of(2025, 1, 1), 1));
            batch.add(new Emp("批量-乙", "男", "批量测试部", "临时岗",
                    new BigDecimal("9100"), LocalDate.of(2025, 1, 2), 1));

            // 注意：foreach 批量插入不会回填主键，需回查后再删除
            assertEquals(2, mapper.addInBatch(batch));
            session.commit();

            Emp condition = new Emp();
            condition.setDept("批量测试部");
            List<Emp> inserted = mapper.queryByExample(condition);
            assertEquals(2, inserted.size());

            List<Integer> ids = new ArrayList<>();
            for (Emp e : inserted) {
                assertNotNull(e.getEmpId());
                ids.add(e.getEmpId());
            }
            assertEquals(2, mapper.removeInBatch(ids));
            session.commit();
            assertTrue(mapper.queryByExample(condition).isEmpty());
        }
    }

    @Test
    @DisplayName("步骤3.4：choose/when/otherwise 分支查询")
    void testQueryByPriority() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);

            // 命中第一分支：按姓名精确查询
            Emp byName = new Emp();
            byName.setEmpName("王强");
            List<Emp> byNameList = mapper.queryByPriority(byName);
            assertEquals(1, byNameList.size());
            assertEquals("王强", byNameList.get(0).getEmpName());

            // 未命中姓名 → 命中第二分支：按部门查询
            Emp byDept = new Emp();
            byDept.setDept("人事部");
            List<Emp> byDeptList = mapper.queryByPriority(byDept);
            assertTrue(byDeptList.size() >= 1);
            byDeptList.forEach(e -> assertEquals("人事部", e.getDept()));

            // 都不命中 → otherwise：查询在职员工
            List<Emp> other = mapper.queryByPriority(new Emp());
            other.forEach(e -> assertEquals(1, e.getStatus()));
            assertEquals(mapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Emp>()
                            .eq(Emp::getStatus, 1)), (long) other.size());
        }
    }
}
