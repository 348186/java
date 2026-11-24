package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.PhotoWorkPlus;
import com.lab.mapper.PhotoWorkPlusMapper;
import com.lab.util.MybatisPlusUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * MyBatis-Plus 通用 CRUD / 条件构造器 / 分页插件测试。
 */
public class PhotoWorkPlusTest {
    private SqlSession session;
    private PhotoWorkPlusMapper mapper;

    @Before
    public void setUp() {
        session = MybatisPlusUtil.openSession();
        mapper = session.getMapper(PhotoWorkPlusMapper.class);
    }

    @After
    public void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    public void testBaseMapperCrud() {
        PhotoWorkPlus work = new PhotoWorkPlus("MP测试作品", "程野", "街拍", "测试外景",
                new java.util.Date(), new BigDecimal("1.0"), new BigDecimal("300.00"), 1);
        try {
            assertEquals(1, mapper.insert(work));
            session.commit();
            assertNotNull(work.getWorkId());
            assertNotNull(mapper.selectById(work.getWorkId()));

            work.setPrice(new BigDecimal("350.00"));
            assertEquals(1, mapper.updateById(work));
            session.commit();
            assertEquals(new BigDecimal("350.00"), mapper.selectById(work.getWorkId()).getPrice());
        } finally {
            if (work.getWorkId() != null) {
                mapper.deleteById(work.getWorkId());
                session.commit();
            }
        }
    }

    @Test
    public void testQueryWrapper() {
        QueryWrapper<PhotoWorkPlus> qw = new QueryWrapper<>();
        qw.like("work_title", "光").or().eq("shoot_type", "人像");
        List<PhotoWorkPlus> list = mapper.selectList(qw);
        assertNotNull(list);
        for (PhotoWorkPlus w : list) {
            assertTrue(w.getWorkTitle().contains("光") || "人像".equals(w.getShootType()));
        }
    }

    @Test
    public void testPagination() {
        IPage<PhotoWorkPlus> page = mapper.selectPage(new Page<>(1, 2), null);
        assertTrue(page.getTotal() >= 6);
        assertEquals(2, page.getRecords().size());
        assertTrue(page.getPages() >= 3);
    }
}
