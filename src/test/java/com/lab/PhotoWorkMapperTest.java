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
 * 每个用例自己开启会话，结束时清理新增数据，避免污染实验数据。
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

    @Test
    public void testSelectAll() {
        List<PhotoWork> list = mapper.selectAll();
        assertNotNull(list);
        assertTrue("摄影作品表不应为空", list.size() >= 6);
        assertNotNull(list.get(0).getWorkTitle());
    }

    @Test
    public void testInsertAndSelectById() throws Exception {
        PhotoWork work = new PhotoWork("JUnit测试作品", "测试摄影师", "静物", "测试影棚",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-01"),
                new BigDecimal("1.0"), new BigDecimal("500.00"), 1);
        try {
            assertEquals(1, mapper.insert(work));
            session.commit();
            assertNotNull("主键应回填", work.getWorkId());

            PhotoWork found = mapper.selectById(work.getWorkId());
            assertNotNull(found);
            assertEquals("JUnit测试作品", found.getWorkTitle());
            assertEquals(new BigDecimal("500.00"), found.getPrice());
        } finally {
            if (work.getWorkId() != null) {
                mapper.deleteById(work.getWorkId());
                session.commit();
            }
        }
    }

    @Test
    public void testUpdate() throws Exception {
        PhotoWork work = new PhotoWork("待更新作品", "程野", "街拍", "测试外景",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-02"),
                new BigDecimal("2.0"), new BigDecimal("600.00"), 1);
        try {
            mapper.insert(work);
            session.commit();

            work.setWorkTitle("已更新作品");
            work.setPrice(new BigDecimal("700.00"));
            assertEquals(1, mapper.update(work));
            session.commit();

            assertEquals("已更新作品", mapper.selectById(work.getWorkId()).getWorkTitle());
            assertEquals(new BigDecimal("700.00"), mapper.selectById(work.getWorkId()).getPrice());
        } finally {
            if (work.getWorkId() != null) {
                mapper.deleteById(work.getWorkId());
                session.commit();
            }
        }
    }

    @Test
    public void testDynamicCondition() {
        PhotoWork cond = new PhotoWork();
        cond.setShootType("人像");
        cond.setStatus(1);
        List<PhotoWork> list = mapper.selectByCondition(cond);
        assertFalse(list.isEmpty());
        for (PhotoWork w : list) {
            assertEquals("人像", w.getShootType());
            assertEquals(Integer.valueOf(1), w.getStatus());
        }

        PhotoWork cond2 = new PhotoWork();
        cond2.setPhotographer("不存在的摄影师");
        assertTrue(mapper.selectByCondition(cond2).isEmpty());
    }

    @Test
    public void testUpdateSelective() throws Exception {
        PhotoWork work = new PhotoWork("局部更新作品", "林越", "风光", "测试外景",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-03"),
                new BigDecimal("3.0"), new BigDecimal("800.00"), 1);
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
            assertEquals("局部更新作品", actual.getWorkTitle()); // 未传字段保持不变
        } finally {
            if (work.getWorkId() != null) {
                mapper.deleteById(work.getWorkId());
                session.commit();
            }
        }
    }

    @Test
    public void testBatchInsertAndDelete() throws Exception {
        PhotoWork a = new PhotoWork("批量A", "苏晴", "风光", "影棚A",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-04"),
                new BigDecimal("1.0"), new BigDecimal("100.00"), 1);
        PhotoWork b = new PhotoWork("批量B", "苏晴", "风光", "影棚B",
                new SimpleDateFormat("yyyy-MM-dd").parse("2025-08-05"),
                new BigDecimal("2.0"), new BigDecimal("200.00"), 1);
        try {
            assertEquals(2, mapper.insertBatch(Arrays.asList(a, b)));
            session.commit();
            assertNotNull(a.getWorkId());
            assertNotNull(b.getWorkId());

            assertEquals(2, mapper.deleteByIds(new Integer[]{a.getWorkId(), b.getWorkId()}));
            session.commit();
        } finally {
            if (a.getWorkId() != null && mapper.selectById(a.getWorkId()) != null) {
                mapper.deleteById(a.getWorkId());
                session.commit();
            }
            if (b.getWorkId() != null && mapper.selectById(b.getWorkId()) != null) {
                mapper.deleteById(b.getWorkId());
                session.commit();
            }
        }
    }

    @Test
    public void testGroupByShootType() {
        assertFalse(mapper.groupByShootType().isEmpty());
    }
}
