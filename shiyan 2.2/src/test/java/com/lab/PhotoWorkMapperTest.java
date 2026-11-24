package com.lab;

import com.lab.entity.PhotoWork;
import com.lab.mapper.PhotoWorkMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 手写 XML Mapper 的 JUnit4 单元测试。
 */
public class PhotoWorkMapperTest {
    private SqlSession session;
    private PhotoWorkMapper mapper;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(PhotoWorkMapper.class);
    }

    @After
    public void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    private PhotoWork sample(String title) throws Exception {
        return new PhotoWork(1, title, "上海 测试路",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-01"),
                "风光", new BigDecimal("500.00"), 1);
    }

    private void clean(PhotoWork work) {
        if (work != null && work.getWorkId() != null && mapper.selectById(work.getWorkId()) != null) {
            mapper.deleteById(work.getWorkId());
            session.commit();
        }
    }

    @Test
    public void testSelectAll() {
        List<PhotoWork> list = mapper.selectAll();
        assertNotNull(list);
        assertTrue("摄影作品表不应为空", list.size() >= 6);
        assertNotNull(list.get(0).getWorkTitle());
        assertNotNull("联表查询应带出摄影师姓名", list.get(0).getPhotographerName());
    }

    @Test
    public void testInsertAndSelectById() throws Exception {
        PhotoWork work = sample("JUnit测试作品");
        try {
            assertEquals(1, mapper.insert(work));
            session.commit();
            assertNotNull("主键应回填", work.getWorkId());

            PhotoWork found = mapper.selectById(work.getWorkId());
            assertNotNull(found);
            assertEquals("JUnit测试作品", found.getWorkTitle());
            assertEquals(new BigDecimal("500.00"), found.getPrice());
            assertEquals(Integer.valueOf(1), found.getPhotographerId());
        } finally {
            clean(work);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        PhotoWork work = sample("待更新作品");
        try {
            mapper.insert(work);
            session.commit();

            work.setWorkTitle("已更新作品");
            work.setPrice(new BigDecimal("700.00"));
            assertEquals(1, mapper.update(work));
            session.commit();

            PhotoWork actual = mapper.selectById(work.getWorkId());
            assertEquals("已更新作品", actual.getWorkTitle());
            assertEquals(new BigDecimal("700.00"), actual.getPrice());
        } finally {
            clean(work);
        }
    }

    @Test
    public void testDynamicCondition() {
        PhotoWork cond = new PhotoWork();
        cond.setStyle("人像");
        cond.setStatus(1);
        List<PhotoWork> list = mapper.selectByCondition(cond);
        assertFalse(list.isEmpty());
        for (PhotoWork w : list) {
            assertEquals("人像", w.getStyle());
            assertEquals(Integer.valueOf(1), w.getStatus());
        }

        PhotoWork cond2 = new PhotoWork();
        cond2.setWorkTitle("不存在的作品标题");
        assertTrue(mapper.selectByCondition(cond2).isEmpty());
    }

    @Test
    public void testUpdateSelective() throws Exception {
        PhotoWork work = sample("局部更新作品");
        try {
            mapper.insert(work);
            session.commit();

            PhotoWork patch = new PhotoWork();
            patch.setWorkId(work.getWorkId());
            patch.setPrice(new BigDecimal("888.00"));
            assertEquals(1, mapper.updateSelective(patch));
            session.commit();

            PhotoWork actual = mapper.selectById(work.getWorkId());
            assertEquals(new BigDecimal("888.00"), actual.getPrice());
            assertEquals("局部更新作品", actual.getWorkTitle());
        } finally {
            clean(work);
        }
    }

    @Test
    public void testBatchInsertAndDelete() throws Exception {
        PhotoWork a = sample("批量A");
        PhotoWork b = sample("批量B");
        try {
            assertEquals(2, mapper.insertBatch(Arrays.asList(a, b)));
            session.commit();
            assertNotNull(a.getWorkId());
            assertNotNull(b.getWorkId());

            assertEquals(2, mapper.deleteByIds(new Integer[]{a.getWorkId(), b.getWorkId()}));
            session.commit();
        } finally {
            clean(a);
            clean(b);
        }
    }

    @Test
    public void testSelectByChoice() {
        assertFalse(mapper.selectByChoice("风光", null).isEmpty());
        assertFalse(mapper.selectByChoice(null, 1).isEmpty());
        assertFalse(mapper.selectByChoice(null, null).isEmpty());
    }

    @Test
    public void testGroupByStyle() {
        assertFalse(mapper.groupByStyle().isEmpty());
    }
}
