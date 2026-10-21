package com.lab;

import com.lab.cache.CacheSessionFactory;
import com.lab.cache.PhotographerCacheMapper;
import com.lab.cache.SqlAccessCounter;
import com.lab.entity.Photographer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 实验三 · MyBatis 缓存机制自动化测试（XML 部分）。
 *
 * <p>核心思路：<strong>用「有没有真的发 SQL」来证明缓存是否命中</strong>。
 * 借助 {@link SqlAccessCounter} 统计 MyBatis 打印的 {@code ==>  Preparing} 条数，
 * 从而把「肉眼看日志」变成可断言的自动化测试。
 *
 * <p>每新建一个 SessionFactory 都会得到一份全新的二级缓存，因此各测试之间天然隔离。
 */
public class PhotographerCacheTest {

    private static final String NS = PhotographerCacheMapper.class.getName();

    // ========================= 一级缓存 =========================

    @Test
    // 一级缓存：同一 SqlSession 内重复查询只发 1 次 SQL，且返回同一对象引用
    public void levelOneCacheReuseInSameSession() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);

            counter.reset();
            Photographer first = mapper.findByPrimaryKey(1);
            Photographer second = mapper.findByPrimaryKey(1);

            assertEquals("首次应查库，第二次应命中一级缓存", 1, counter.sqlCount());
            assertSame("一级缓存保存的是对象引用，两次应拿到同一个对象", first, second);
        }
    }

    @Test
    // 一级缓存：跨 SqlSession 不共享
    public void levelOneCacheNotSharedBetweenSessions() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            SqlSession session1 = factory.openSession();
            SqlSession session2 = factory.openSession();
            try {
                counter.reset();
                session1.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
                session2.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
                assertEquals("两个 SqlSession 各有独立的本地缓存", 2, counter.sqlCount());
            } finally {
                session1.close();
                session2.close();
            }
        }
    }

    @Test
    // 一级缓存：写操作、clearCache、commit 均会清空本地缓存
    public void levelOneCacheInvalidation() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            Photographer photographer = mapper.findByPrimaryKey(2);
            BigDecimal origin = photographer.getDayRate();

            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals("第二次应命中一级缓存", 0, counter.sqlCount());

            // 1）增删改清空一级缓存（写完回滚，保证数据不变）
            mapper.renewDayRate(2, origin);
            session.rollback();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals("update 后一级缓存应失效", 1, counter.sqlCount());

            // 2）clearCache 清空
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(0, counter.sqlCount());
            session.clearCache();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals("clearCache 后应重新查库", 1, counter.sqlCount());

            // 3）commit 清空
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(0, counter.sqlCount());
            session.commit();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals("commit 后应重新查库", 1, counter.sqlCount());
        }
    }

    @Test
    // 一级缓存：localCacheScope=STATEMENT 时同 Session 内也不复用
    public void levelOneCacheUnderStatementScope() {
        SqlSessionFactory factory = CacheSessionFactory.newStatementScopeFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);

            counter.reset();
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(1);
            assertEquals("STATEMENT 作用域下语句结束即清缓存", 2, counter.sqlCount());
        }
    }

    // ========================= 二级缓存 =========================

    @Test
    // 二级缓存：跨 SqlSession 共享，且返回的是反序列化副本而非同一对象
    public void levelTwoCacheSharedAndReturnsCopy() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            Photographer first;
            try (SqlSession session = factory.openSession()) {
                first = session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            } // close() 时数据被提交到二级缓存

            Photographer second;
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                second = session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals("第二个 Session 应命中二级缓存，不再查库", 0, counter.sqlCount());
            assertNotNull(second);
            assertNotSame("二级缓存返回的是副本，不应是同一个对象", first, second);
            assertEquals(first.getPhotographerId(), second.getPhotographerId());
            assertEquals(first.getDayRate(), second.getDayRate());
            assertTrue("二级缓存中应有条目", factory.getConfiguration().getCache(NS).getSize() > 0);
        }
    }

    @Test
    // 二级缓存：前一个 Session 未 close/commit 则不会提交到二级缓存
    public void levelTwoCacheRequiresCommitOrClose() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            SqlSession session1 = factory.openSession();
            session1.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);

            counter.reset();
            try (SqlSession session2 = factory.openSession()) {
                session2.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);
            }
            assertEquals("未提交前，其他 Session 查不到缓存", 1, counter.sqlCount());

            session1.close();
            counter.reset();
            try (SqlSession session3 = factory.openSession()) {
                session3.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);
            }
            assertEquals("close() 之后应命中二级缓存", 0, counter.sqlCount());
        }
    }

    @Test
    // 二级缓存：写操作提交后清空本 namespace 的缓存
    public void levelTwoCacheClearedAfterWrite() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(4);
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(4);
            }
            assertEquals("预热后应命中二级缓存", 0, counter.sqlCount());

            BigDecimal origin;
            try (SqlSession session = factory.openSession()) {
                PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
                origin = mapper.findByPrimaryKey(4).getDayRate();
                mapper.renewDayRate(4, origin);   // 写回原值，数据不变
                session.commit();
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(4);
            }
            assertEquals("写操作提交应清空本 namespace 的二级缓存", 1, counter.sqlCount());
        }
    }

    @Test
    // useCache=false：查询结果不进入二级缓存
    public void useCacheFalseIsNeverCached() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findAll();
            }
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findAll();
            }
            assertEquals("useCache=false 时每次都应查库", 2, counter.sqlCount());
        }
    }

    @Test
    // flushCache=true 的 select 会清空二级缓存
    public void flushCacheTrueClearsCache() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals("预热后应命中二级缓存", 0, counter.sqlCount());

            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findAllFresh();
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals("findAllFresh 执行前应已清空二级缓存", 1, counter.sqlCount());
        }
    }

    @Test
    // CacheKey：不同 SQL / 不同参数互不共享
    public void cacheKeyDiffersByStatementAndParam() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
                mapper.findByPrimaryKey(1);
                mapper.findByPrimaryKey(2);
                mapper.findByStudio("人像工作室");
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
                mapper.findByPrimaryKey(1);
                mapper.findByPrimaryKey(2);
                mapper.findByStudio("人像工作室");
            }
            assertEquals("完全相同的语句与参数应全部命中", 0, counter.sqlCount());

            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(PhotographerCacheMapper.class).findByStudio("婚礼纪实组");
            }
            assertEquals("换成新参数应重新查库", 1, counter.sqlCount());
        }
    }
}
