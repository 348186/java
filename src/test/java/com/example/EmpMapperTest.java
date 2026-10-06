package com.example;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Emp;
import com.example.mapper.EmpMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmpMapperTest {

    private SqlSession sqlSession;
    private EmpMapper empMapper;

    @BeforeEach
    void setUp() throws IOException {
        InputStream is = Resources.getResourceAsStream("chapper01/mabatis.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        sqlSession = sqlSessionFactory.openSession();
        empMapper = sqlSession.getMapper(EmpMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @Test
    @Order(1)
    void testSelectById() {
        System.out.println("========== 根据 ID 查询 ==========");
        Emp emp = empMapper.selectById(1);
        assertNotNull(emp);
        System.out.println(emp);
    }

    @Test
    @Order(2)
    void testSelectList() {
        System.out.println("========== 查询所有在职员工 ==========");
        List<Emp> list = empMapper.selectList(null);
        System.out.println("共 " + list.size() + " 条记录:");
        list.forEach(System.out::println);
    }

    @Test
    @Order(3)
    void testQueryWrapper() {
        System.out.println("========== 条件构造器: 研发部 + 薪资>=10000 降序 ==========");
        QueryWrapper<Emp> wrapper = new QueryWrapper<>();
        wrapper.eq("dept", "研发部")
               .ge("salary", 10000)
               .orderByDesc("salary");
        List<Emp> list = empMapper.selectList(wrapper);
        System.out.println("查询结果:");
        list.forEach(System.out::println);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(4)
    void testLambdaQueryWrapper() {
        System.out.println("========== Lambda 条件构造器: 在职 + 模糊名 ==========");
        LambdaQueryWrapper<Emp> lw = new LambdaQueryWrapper<>();
        lw.eq(Emp::getStatus, 1)
          .like(Emp::getEmpName, "张")
          .orderByDesc(Emp::getSalary);
        List<Emp> list = empMapper.selectList(lw);
        System.out.println("查询结果:");
        list.forEach(System.out::println);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(5)
    void testInsert() {
        System.out.println("========== 新增员工 ==========");
        Emp emp = new Emp();
        emp.setEmpName("陈新");
        emp.setGender("男");
        emp.setDept("测试部");
        emp.setPost("测试工程师");
        emp.setSalary(new BigDecimal("9500.00"));
        emp.setHireDate(new Date());
        emp.setStatus(1);
        int rows = empMapper.insert(emp);
        sqlSession.commit();
        assertTrue(rows > 0);
        assertNotNull(emp.getEmpId());
        System.out.println("插入成功, 自增主键 = " + emp.getEmpId());
    }

    @Test
    @Order(6)
    void testUpdateById() {
        System.out.println("========== 根据 ID 更新 ==========");
        Emp emp = empMapper.selectById(1);
        assertNotNull(emp);
        emp.setSalary(new BigDecimal("13000.00"));
        int rows = empMapper.updateById(emp);
        sqlSession.commit();
        assertTrue(rows > 0);
        Emp updated = empMapper.selectById(1);
        System.out.println("更新后: " + updated);
    }

    @Test
    @Order(7)
    void testSelectPage() {
        System.out.println("========== 分页查询: 第1页, 每页2条 ==========");
        Page<Emp> page = new Page<>(1, 2);
        QueryWrapper<Emp> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("emp_id");
        Page<Emp> result = empMapper.selectPage(page, wrapper);
        System.out.println("总记录数 = " + result.getTotal());
        System.out.println("总页数 = " + result.getPages());
        System.out.println("当前页数据:");
        result.getRecords().forEach(System.out::println);
        assertTrue(result.getRecords().size() <= 2);
    }

    @Test
    @Order(8)
    void testDeleteById() {
        System.out.println("========== 逻辑删除(状态置为0) ==========");
        Page<Emp> countPage = new Page<>(1, 1);
        Page<Emp> before = empMapper.selectPage(countPage, null);
        long totalBefore = before.getTotal();

        Emp emp = new Emp();
        emp.setEmpName("待删除");
        emp.setGender("女");
        emp.setDept("临时部");
        emp.setPost("临时岗");
        emp.setSalary(new BigDecimal("5000"));
        emp.setStatus(1);
        empMapper.insert(emp);
        sqlSession.commit();
        Integer newId = emp.getEmpId();
        System.out.println("新增记录 ID = " + newId + " ,当前总记录 = " + (totalBefore + 1));

        int rows = empMapper.deleteById(newId);
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("deleteById 执行完毕 (逻辑删除, status -> 0)");

        Emp deleted = empMapper.selectById(newId);
        assertNull(deleted);
        System.out.println("selectById 查不到该记录 -> 逻辑删除生效");

        QueryWrapper<Emp> wrapper = new QueryWrapper<>();
        wrapper.eq("emp_id", newId);
        List<Emp> all = empMapper.selectList(wrapper);
        assertTrue(all.isEmpty());
        System.out.println("selectList 也查不到 -> OK");
    }
}