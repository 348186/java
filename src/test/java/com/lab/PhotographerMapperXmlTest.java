package com.lab;

import com.lab.entity.Photographer;
import com.lab.mapper.PhotographerMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 实验一 · 原生 MyBatis 单元测试（任务 2、任务 3）。
 *
 * <p>设计约定：所有写操作在测试结束时自行还原，保证 photographer 表数据始终是
 * 「指导书 4 条 + 扩充 10 条」的固定集合。
 * 断言尽量不写死行数，而是与 MP 的 selectCount() 交叉校验，
 * 这样后续再增删数据时测试不会失效。
 */
public class PhotographerMapperXmlTest {

    private Photographer buildTphotographerPhotographer(String name) {
        return new Photographer(name, "女", "旅拍小队", "临时领域",
                new BigDecimal("9000"), LocalDate.of(2025, 1, 1), 1);
    }

    @Test
    // 步骤2.4：查询全部摄影师，验证指导书 4 条数据均存在
    public void testFindAll() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            List<Photographer> list = mapper.findAll();
            list.forEach(System.out::println);

            // 与 MP 通用方法交叉校验，避免把行数写死
            assertEquals((long) mapper.selectCount(null), (long) list.size());

            // 指导书给定的 4 条数据必须存在
            List<String> names = new ArrayList<>();
            list.forEach(e -> names.add(e.getPhotographerName()));
            assertTrue(names.containsAll(List.of("张伟", "李娜", "王强", "赵敏")));
        }
    }

    @Test
    // 步骤2.3：按主键查询，验证结果映射
    public void testFindByPrimaryKey() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            Photographer photographer = session.getMapper(PhotographerMapper.class).findByPrimaryKey(1);
            assertNotNull(photographer);
            assertEquals("张伟", photographer.getPhotographerName());
            assertEquals("人像工作室", photographer.getStudio());
            assertEquals(LocalDate.of(2024, 7, 1), photographer.getJoinDate());
        }
    }

    @Test
    // 步骤2.3：新增并回填自增主键，随后清理
    public void testAddPhotographer() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            Photographer photographer = buildTphotographerPhotographer("临时-新增");
            int rows = mapper.addPhotographer(photographer);
            session.commit();
            assertEquals(1, rows);
            assertNotNull("useGeneratedKeys 应回填主键", photographer.getPhotographerId());
            System.out.println("回填主键 photographerId = " + photographer.getPhotographerId());

            // 还原数据
            mapper.removePhotographer(photographer.getPhotographerId());
            session.commit();
            assertNull(mapper.findByPrimaryKey(photographer.getPhotographerId()));
        }
    }

    @Test
    // 步骤2.3：修改摄影师日薪，随后还原
    public void testModifyPhotographer() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            Photographer photographer = mapper.findByPrimaryKey(2);
            BigDecimal origin = photographer.getDayRate();

            photographer.setDayRate(new BigDecimal("13500"));
            assertEquals(1, mapper.modifyPhotographer(photographer));
            session.commit();
            assertEquals(0, new BigDecimal("13500").compareTo(
                    mapper.findByPrimaryKey(2).getDayRate()));

            // 还原数据
            photographer.setDayRate(origin);
            mapper.modifyPhotographer(photographer);
            session.commit();
            assertEquals(0, origin.compareTo(mapper.findByPrimaryKey(2).getDayRate()));
        }
    }

    @Test
    // 步骤3.1：多条件组合查询（if + where）
    public void testQueryByExample() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);

            // 单条件：工作室
            Photographer byStudio = new Photographer();
            byStudio.setStudio("人像工作室");
            List<Photographer> studioList = mapper.queryByExample(byStudio);
            assertTrue(studioList.size() >= 2);
            studioList.forEach(e -> assertEquals("人像工作室", e.getStudio()));
            assertEquals((long) studioList.size(), (long) mapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Photographer>()
                            .eq(Photographer::getStudio, "人像工作室")));

            // 双条件：工作室 + 性别
            Photographer byStudioGender = new Photographer();
            byStudioGender.setStudio("人像工作室");
            byStudioGender.setGender("女");
            List<Photographer> list = mapper.queryByExample(byStudioGender);
            list.forEach(e -> {
                assertEquals("人像工作室", e.getStudio());
                assertEquals("女", e.getGender());
            });
            assertTrue(list.stream().anyMatch(e -> "李娜".equals(e.getPhotographerName())));

            // 日薪下限
            Photographer byDayRate = new Photographer();
            byDayRate.setDayRate(new BigDecimal("12000"));
            mapper.queryByExample(byDayRate).forEach(
                    e -> assertTrue(e.getDayRate().compareTo(new BigDecimal("12000")) >= 0));

            // 无条件时 <where> 不生成 WHERE 子句，等价于全表查询
            assertEquals((long) mapper.selectCount(null),
                    (long) mapper.queryByExample(new Photographer()).size());
        }
    }

    @Test
    // 步骤3.2：动态更新（set + if）只更新非空字段
    public void testModifySelective() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            Photographer photographer = mapper.findByPrimaryKey(3);
            String originStudio = photographer.getStudio();

            Photographer patch = new Photographer();
            patch.setPhotographerId(3);
            patch.setStudio("风光二组");
            assertEquals(1, mapper.modifySelective(patch));
            session.commit();
            assertEquals("风光二组", mapper.findByPrimaryKey(3).getStudio());
            // specialty 为 null，不应被更新
            assertEquals(photographer.getSpecialty(), mapper.findByPrimaryKey(3).getSpecialty());

            // 还原数据
            patch.setStudio(originStudio);
            mapper.modifySelective(patch);
            session.commit();
            assertEquals(originStudio, mapper.findByPrimaryKey(3).getStudio());
        }
    }

    @Test
    // 步骤3.3：批量新增与批量删除（foreach）
    public void testBatch() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            List<Photographer> batch = new ArrayList<>();
            batch.add(new Photographer("批量-甲", "女", "临时验证组", "临时领域",
                    new BigDecimal("9000"), LocalDate.of(2025, 1, 1), 1));
            batch.add(new Photographer("批量-乙", "男", "临时验证组", "临时领域",
                    new BigDecimal("9100"), LocalDate.of(2025, 1, 2), 1));

            // 注意：foreach 批量插入不会回填主键，需回查后再删除
            assertEquals(2, mapper.addInBatch(batch));
            session.commit();

            Photographer condition = new Photographer();
            condition.setStudio("临时验证组");
            List<Photographer> inserted = mapper.queryByExample(condition);
            assertEquals(2, inserted.size());

            List<Integer> ids = new ArrayList<>();
            for (Photographer e : inserted) {
                assertNotNull(e.getPhotographerId());
                ids.add(e.getPhotographerId());
            }
            assertEquals(2, mapper.removeInBatch(ids));
            session.commit();
            assertTrue(mapper.queryByExample(condition).isEmpty());
        }
    }

    @Test
    // 步骤3.4：choose/when/otherwise 分支查询
    public void testQueryByPriority() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);

            // 命中第一分支：按姓名精确查询
            Photographer byName = new Photographer();
            byName.setPhotographerName("王强");
            List<Photographer> byNameList = mapper.queryByPriority(byName);
            assertEquals(1, byNameList.size());
            assertEquals("王强", byNameList.get(0).getPhotographerName());

            // 未命中姓名 → 命中第二分支：按工作室查询
            Photographer byStudio = new Photographer();
            byStudio.setStudio("商业广告组");
            List<Photographer> byStudioList = mapper.queryByPriority(byStudio);
            assertTrue(byStudioList.size() >= 1);
            byStudioList.forEach(e -> assertEquals("商业广告组", e.getStudio()));

            // 都不命中 → otherwise：查询在职摄影师
            List<Photographer> other = mapper.queryByPriority(new Photographer());
            other.forEach(e -> assertEquals(Integer.valueOf(1), e.getStatus()));
            assertEquals((long) mapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Photographer>()
                            .eq(Photographer::getStatus, 1)), (long) other.size());
        }
    }
}
