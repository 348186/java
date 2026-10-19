package com.lab;

import com.lab.cache.CacheSessionFactory;
import com.lab.cache.EmpCacheMapper;
import com.lab.cache.SqlAccessCounter;
import com.lab.entity.Emp;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实验三 · MyBatis 缓存机制自动化测试（XML 部分）。
 *
 * <p>核心思路：<strong>用「有没有真的发 SQL」来证明缓存是否命中</strong>。
 * 借助 {@link SqlAccessCounter} 统计 MyBatis 打印的 {@code ==>  Preparing} 条数，
 * 从而把「肉眼看日志」变成可断言的自动化测试。
 *
 * <p>每新建一个 SessionFactory 都会得到一份全新的二级缓存，因此各测试之间天然隔离。
 */
class EmpCacheTest {

    private static final String NS = EmpCacheMapper.class.getName();

    // ========================= 一级缓存 =========================

    @Test
    @DisplayName("一级缓存：同一 SqlSession 内重复查询只发 1 次 SQL，且返回同一对象引用")
    void levelOneCacheReuseInSameSession() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);

            counter.reset();
            Emp first = mapper.findByPrimaryKey(1);
            Emp second = mapper.findByPrimaryKey(1);

            assertEquals(1, counter.sqlCount(), "首次应查库，第二次应命中一级缓存");
            assertSame(first, second, "一级缓存保存的是对象引用，两次应拿到同一个对象");
        }
    }

    @Test
    @DisplayName("一级缓存：跨 SqlSession 不共享")
    void levelOneCacheNotSharedBetweenSessions() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            SqlSession session1 = factory.openSession();
            SqlSession session2 = factory.openSession();
            try {
                counter.reset();
                session1.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
                session2.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
                assertEquals(2, counter.sqlCount(), "两个 SqlSession 各有独立的本地缓存");
            } finally {
                session1.close();
                session2.close();
            }
        }
    }

    @Test
    @DisplayName("一级缓存：写操作、clearCache、commit 均会清空本地缓存")
    void levelOneCacheInvalidation() {
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);
            Emp emp = mapper.findByPrimaryKey(2);
            BigDecimal origin = emp.getSalary();

            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(0, counter.sqlCount(), "第二次应命中一级缓存");

            // 1）增删改清空一级缓存（写完回滚，保证数据不变）
            mapper.renewSalary(2, origin);
            session.rollback();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(1, counter.sqlCount(), "update 后一级缓存应失效");

            // 2）clearCache 清空
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(0, counter.sqlCount());
            session.clearCache();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(1, counter.sqlCount(), "clearCache 后应重新查库");

            // 3）commit 清空
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(0, counter.sqlCount());
            session.commit();
            counter.reset();
            mapper.findByPrimaryKey(2);
            assertEquals(1, counter.sqlCount(), "commit 后应重新查库");
        }
    }

    @Test
    @DisplayName("一级缓存：localCacheScope=STATEMENT 时同 Session 内也不复用")
    void levelOneCacheUnderStatementScope() {
        SqlSessionFactory factory = CacheSessionFactory.newStatementScopeFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache");
             SqlSession session = factory.openSession()) {
            EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);

            counter.reset();
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(1);
            assertEquals(2, counter.sqlCount(), "STATEMENT 作用域下语句结束即清缓存");
        }
    }

    // ========================= 二级缓存 =========================

    @Test
    @DisplayName("二级缓存：跨 SqlSession 共享，且返回的是反序列化副本而非同一对象")
    void levelTwoCacheSharedAndReturnsCopy() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            Emp first;
            try (SqlSession session = factory.openSession()) {
                first = session.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
            } // close() 时数据被提交到二级缓存

            Emp second;
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                second = session.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals(0, counter.sqlCount(), "第二个 Session 应命中二级缓存，不再查库");
            assertNotNull(second);
            assertNotSame(first, second, "二级缓存返回的是副本，不应是同一个对象");
            assertEquals(first.getEmpId(), second.getEmpId());
            assertEquals(first.getSalary(), second.getSalary());
            assertTrue(factory.getConfiguration().getCache(NS).getSize() > 0, "二级缓存中应有条目");
        }
    }

    @Test
    @DisplayName("二级缓存：前一个 Session 未 close/commit 则不会提交到二级缓存")
    void levelTwoCacheRequiresCommitOrClose() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            SqlSession session1 = factory.openSession();
            session1.getMapper(EmpCacheMapper.class).findByPrimaryKey(3);

            counter.reset();
            try (SqlSession session2 = factory.openSession()) {
                session2.getMapper(EmpCacheMapper.class).findByPrimaryKey(3);
            }
            assertEquals(1, counter.sqlCount(), "未提交前，其他 Session 查不到缓存");

            session1.close();
            counter.reset();
            try (SqlSession session3 = factory.openSession()) {
                session3.getMapper(EmpCacheMapper.class).findByPrimaryKey(3);
            }
            assertEquals(0, counter.sqlCount(), "close() 之后应命中二级缓存");
        }
    }

    @Test
    @DisplayName("二级缓存：写操作提交后清空本 namespace 的缓存")
    void levelTwoCacheClearedAfterWrite() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(4);
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(4);
            }
            assertEquals(0, counter.sqlCount(), "预热后应命中二级缓存");

            BigDecimal origin;
            try (SqlSession session = factory.openSession()) {
                EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);
                origin = mapper.findByPrimaryKey(4).getSalary();
                mapper.renewSalary(4, origin);   // 写回原值，数据不变
                session.commit();
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(4);
            }
            assertEquals(1, counter.sqlCount(), "写操作提交应清空本 namespace 的二级缓存");
        }
    }

    @Test
    @DisplayName("useCache=false：查询结果不进入二级缓存")
    void useCacheFalseIsNeverCached() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findAll();
            }
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findAll();
            }
            assertEquals(2, counter.sqlCount(), "useCache=false 时每次都应查库");
        }
    }

    @Test
    @DisplayName("flushCache=true 的 select 会清空二级缓存")
    void flushCacheTrueClearsCache() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals(0, counter.sqlCount(), "预热后应命中二级缓存");

            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findAllFresh();
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByPrimaryKey(1);
            }
            assertEquals(1, counter.sqlCount(), "findAllFresh 执行前应已清空二级缓存");
        }
    }

    @Test
    @DisplayName("CacheKey：不同 SQL / 不同参数互不共享")
    void cacheKeyDiffersByStatementAndParam() {
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            try (SqlSession session = factory.openSession()) {
                EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);
                mapper.findByPrimaryKey(1);
                mapper.findByPrimaryKey(2);
                mapper.findByDept("研发部");
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                EmpCacheMapper mapper = session.getMapper(EmpCacheMapper.class);
                mapper.findByPrimaryKey(1);
                mapper.findByPrimaryKey(2);
                mapper.findByDept("研发部");
            }
            assertEquals(0, counter.sqlCount(), "完全相同的语句与参数应全部命中");

            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheMapper.class).findByDept("财务部");
            }
            assertEquals(1, counter.sqlCount(), "换成新参数应重新查库");
        }
    }
}
