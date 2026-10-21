package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.Photographer;
import com.lab.mapper.PhotographerMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 实验一 · MyBatis-Plus 单元测试（任务 4）。
 * BaseMapper 通用 CRUD、条件构造器、分页插件。
 * 断言基于「当前数据集合」动态推导，数据扩充后无需修改测试。
 */
public class PhotographerMybatisPlusTest {

    @Test
    // 步骤4.3：BaseMapper 通用 CRUD —— 查询与统计
    public void testBaseCrud() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);

            long total = mapper.selectCount(null);
            assertTrue("photographer 表至少应包含指导书给定的 4 条数据", total >= 4);
            System.out.println("当前摄影师总数 = " + total);

            Photographer photographer = mapper.selectById(1);
            assertNotNull(photographer);
            assertEquals("张伟", photographer.getPhotographerName());

            // 扩充数据同样可被查询到
            List<Photographer> all = mapper.selectList(null);
            List<String> names = new ArrayList<>();
            all.forEach(e -> names.add(e.getPhotographerName()));
            assertTrue(names.containsAll(Arrays.asList("张三", "李四", "刘岩", "权小乐")));
        }
    }

    @Test
    // 步骤4.3：BaseMapper 新增 / 修改 / 删除，随后还原
    public void testBaseWrite() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            long before = mapper.selectCount(null);

            Photographer photographer = new Photographer("临时-MP", "男", "旅拍小队", "临时领域",
                    new BigDecimal("8800"), LocalDate.of(2025, 3, 1), 1);

            assertEquals(1, mapper.insert(photographer));
            session.commit();
            assertNotNull(photographer.getPhotographerId());

            photographer.setDayRate(new BigDecimal("9900"));
            assertEquals(1, mapper.updateById(photographer));
            session.commit();
            assertEquals(0, new BigDecimal("9900").compareTo(
                    mapper.selectById(photographer.getPhotographerId()).getDayRate()));

            assertEquals(1, mapper.deleteById(photographer.getPhotographerId()));
            session.commit();
            assertEquals("删除后记录数应回到初始值", before, (long) mapper.selectCount(null));
        }
    }

    @Test
    // 步骤4.4：QueryWrapper 多条件 + 排序
    public void testQueryWrapper() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            QueryWrapper<Photographer> wrapper = new QueryWrapper<Photographer>()
                    .eq("status", 1)
                    .ge("day_rate", new BigDecimal("9000"))
                    .orderByDesc("day_rate");
            List<Photographer> list = mapper.selectList(wrapper);
            list.forEach(System.out::println);

            // 结果集必须与条件自洽，且按日薪倒序
            assertTrue(list.size() > 0);
            BigDecimal previous = null;
            for (Photographer e : list) {
                assertEquals(Integer.valueOf(1), e.getStatus());
                assertTrue(e.getDayRate().compareTo(new BigDecimal("9000")) >= 0);
                if (previous != null) {
                    assertTrue("应按日薪倒序排列", previous.compareTo(e.getDayRate()) >= 0);
                }
                previous = e.getDayRate();
            }
            assertEquals((long) list.size(), (long) mapper.selectCount(wrapper));
        }
    }

    @Test
    // 步骤4.4：LambdaQueryWrapper 写法，防止字段名写错
    public void testLambdaQueryWrapper() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            LambdaQueryWrapper<Photographer> wrapper = new LambdaQueryWrapper<Photographer>()
                    .eq(Photographer::getStudio, "人像工作室")
                    .eq(Photographer::getStatus, 1)
                    .orderByAsc(Photographer::getPhotographerId);
            List<Photographer> list = mapper.selectList(wrapper);
            assertTrue(list.size() >= 2);
            list.forEach(e -> {
                assertEquals("人像工作室", e.getStudio());
                assertEquals(Integer.valueOf(1), e.getStatus());
            });
        }
    }

    @Test
    // 步骤4.5：分页插件 —— 第 1 页每页 3 条
    public void testPagination() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            long total = mapper.selectCount(null);

            IPage<Photographer> page = mapper.selectPage(new Page<>(1, 3), null);
            System.out.println("总记录数 = " + page.getTotal());
            System.out.println("总页数   = " + page.getPages());
            page.getRecords().forEach(System.out::println);

            assertEquals("total 应为全表记录数", total, page.getTotal());
            assertEquals("每页应为 3 条", 3, page.getRecords().size());
            assertEquals("页数应按每页 3 条向上取整", (total + 2) / 3, page.getPages());
        }
    }

    @Test
    // 步骤4.3：按 id 集合批量查询
    public void testSelectBatchIds() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            List<Photographer> list = mapper.selectBatchIds(Arrays.asList(1, 2, 3));
            assertEquals(3, list.size());
        }
    }
}
