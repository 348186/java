package com.lab.cache;

import com.lab.entity.Photographer;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.math.BigDecimal;

/**
 * 实验三 · MyBatis 缓存机制 —— XML 方式演示入口。
 *
 * <p>与传统「对着控制台肉眼看日志」不同，本入口借助 {@link SqlAccessCounter}
 * 统计每段代码<strong>实际发出的 SQL 次数</strong>，再与预期次数比对并给出 ✔/✘ 结论，
 * 适合直接把运行结果粘贴进实验报告。
 *
 * <p>全程只对 photographer 表做「用原值更新 + 回滚/提交」的操作，演示结束后数据保持不变。
 */
public class CacheXmlDemo {

    private static final String NS = PhotographerCacheMapper.class.getName();

    public static void main(String[] args) {
        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            levelOneCache(counter);
            levelOneScopeStatement(counter);
            levelTwoCache(counter);
            levelTwoNotCommitted(counter);
            levelTwoInvalidatedByWrite(counter);
            cacheKeyDifference(counter);
        }
    }

    // ------------------------------------------------------------------
    // 一、一级缓存
    // ------------------------------------------------------------------

    /** 一级缓存：同一 SqlSession 内复用，拿到的是同一个对象引用 */
    private static void levelOneCache(SqlAccessCounter counter) {
        banner("一、一级缓存（SqlSession 级，默认开启）");
        SqlSessionFactory factory = CacheSessionFactory.newLevelOneFactory();

        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);

            counter.reset();
            Photographer first = mapper.findByPrimaryKey(1);
            Photographer second = mapper.findByPrimaryKey(1);
            check(counter, "同一 SqlSession 连续两次 findByPrimaryKey(1)", 1);
            System.out.println("    两次结果是否为同一对象引用: " + (first == second)
                    + "    → 一级缓存缓存的是对象本身");

            counter.reset();
            mapper.findByPrimaryKey(1);
            check(counter, "紧接着再查一次", 0);
        }

        System.out.println("\n  【跨 SqlSession 不共享】");
        SqlSession sessionA = factory.openSession();
        SqlSession sessionB = factory.openSession();
        try {
            // 注意：sessionA 故意保持打开状态——一旦 close()，数据就会被提交到二级缓存，
            // 那么 sessionB 就可能直接命中二级缓存，从而掩盖「一级缓存不跨 Session」的结论。
            counter.reset();
            sessionA.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            sessionB.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
            check(counter, "两个 SqlSession 各查一次相同数据", 2);
        } finally {
            sessionA.close();
            sessionB.close();
        }

        System.out.println("\n  【失效场景】");
        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            Photographer target = mapper.findByPrimaryKey(2);
            BigDecimal origin = target.getDayRate();

            mapper.findByPrimaryKey(2);
            counter.reset();
            mapper.findByPrimaryKey(2);
            check(counter, "清空缓存前重复查询", 0);

            // 用原值 update 再 rollback：既触发缓存清除，又不会改动数据
            mapper.renewDayRate(2, origin);
            session.rollback();
            counter.reset();
            mapper.findByPrimaryKey(2);
            check(counter, "执行 update（rollback）后再查", 1);

            counter.reset();
            mapper.findByPrimaryKey(2);
            check(counter, "再次查询（重新进入一级缓存）", 0);

            session.clearCache();
            counter.reset();
            mapper.findByPrimaryKey(2);
            check(counter, "session.clearCache() 后再查", 1);

            session.commit();
            counter.reset();
            mapper.findByPrimaryKey(2);
            check(counter, "session.commit() 后再查", 1);
        }
    }

    /** 对照实验：localCacheScope=STATEMENT 时，同一 Session 内的重复查询也不再复用 */
    private static void levelOneScopeStatement(SqlAccessCounter counter) {
        banner("二、对照实验：localCacheScope = STATEMENT");
        SqlSessionFactory factory = CacheSessionFactory.newStatementScopeFactory();

        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            counter.reset();
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(1);
            check(counter, "同一 SqlSession 连续三次相同查询（STATEMENT 作用域）", 3);
        }
        System.out.println("    → STATEMENT 作用域下语句执行完即清空本地缓存，这是禁用一级缓存的唯一手段");
    }

    // ------------------------------------------------------------------
    // 二、二级缓存
    // ------------------------------------------------------------------

    /** 二级缓存：跨 SqlSession 共享，返回的是反序列化后的副本 */
    private static void levelTwoCache(SqlAccessCounter counter) {
        banner("三、二级缓存（namespace 级，需显式开启）");
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();

        System.out.println("    二级缓存实现: " + describeCache(factory));

        Photographer cached = warmUp(factory, 1);
        System.out.println("    第 1 次查询（Session1）: " + cached.getPhotographerName()
                + "，dayRate = " + cached.getDayRate());

        Photographer second = null;
        counter.reset();
        try (SqlSession session = factory.openSession()) {
            second = session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
        }
        check(counter, "Session1.close() 后，Session2 查询相同数据", 0);
        System.out.println("    两次结果是否为同一对象引用: " + (cached == second)
                + "    → 二级缓存返回的是反序列化出来的副本");
        System.out.println("    " + counter.lastHitRatio());
        System.out.println("    当前缓存中的条目数: " + factory.getConfiguration().getCache(NS).getSize());
    }

    /** 常见踩坑：前一个 Session 没有 close/commit，数据根本没进二级缓存 */
    private static void levelTwoNotCommitted(SqlAccessCounter counter) {
        banner("四、踩坑演示：不 close / 不 commit 就不会提交到二级缓存");
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();

        SqlSession session1 = factory.openSession();
        session1.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);

        counter.reset();
        try (SqlSession session2 = factory.openSession()) {
            session2.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);
        }
        check(counter, "Session1 未关闭时，Session2 查询相同数据", 1);
        System.out.println("    → 二级缓存是在 SqlSession close()/commit() 时才写入的");

        session1.close();
        counter.reset();
        try (SqlSession session3 = factory.openSession()) {
            session3.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(3);
        }
        check(counter, "Session1.close() 之后，Session3 再查", 0);
    }

    /** 二级缓存失效：写操作提交后清空本 namespace 的二级缓存 */
    private static void levelTwoInvalidatedByWrite(SqlAccessCounter counter) {
        banner("五、二级缓存失效：增删改 + useCache/flushCache");
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();
        Cache cache = factory.getConfiguration().getCache(NS);

        warmUp(factory, 4);
        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(4);
        }
        check(counter, "预热后命中二级缓存", 0);

        BigDecimal origin;
        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            origin = mapper.findByPrimaryKey(4).getDayRate();
            // 用原值更新并提交：数据没变，但 flushCache 会清空本 namespace 的二级缓存
            mapper.renewDayRate(4, origin);
            session.commit();
        }
        System.out.println("    执行 renewDayRate（写回原值 " + origin + " 后 commit）");
        System.out.println("    清空后缓存条目数: " + cache.getSize());

        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(4);
        }
        check(counter, "写操作提交后再查", 1);

        System.out.println("\n  【useCache=false 的查询不进二级缓存】");
        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findAll();
        }
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findAll();
        }
        check(counter, "两个 Session 各执行一次 findAll（useCache=false）", 2);

        System.out.println("\n  【flushCache=true 的查询会把二级缓存冲掉】");
        warmUp(factory, 1);
        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
        }
        check(counter, "重新预热后查询", 0);

        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            System.out.println("    执行 findAllFresh（useCache=false + flushCache=true）");
            mapper.findAllFresh();
        }
        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(1);
        }
        check(counter, "findAllFresh 之后再次查询相同数据", 1);
    }

    /**
     * CacheKey 差异：不同 SQL / 不同参数各自独立，互不共享。
     * 缓存 Key = statementId + SQL 文本 + 参数值 + RowBounds 等。
     */
    private static void cacheKeyDifference(SqlAccessCounter counter) {
        banner("六、CacheKey：不同 SQL / 不同参数互不共享");
        SqlSessionFactory factory = CacheSessionFactory.newXmlFactory();

        counter.reset();
        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(2);
            mapper.findByStudio("人像工作室");
        }
        check(counter, "三条不同 SQL / 参数首次执行", 3);

        counter.reset();
        try (SqlSession session = factory.openSession()) {
            PhotographerCacheMapper mapper = session.getMapper(PhotographerCacheMapper.class);
            mapper.findByPrimaryKey(1);
            mapper.findByPrimaryKey(2);
            mapper.findByStudio("人像工作室");
        }
        check(counter, "新 Session 重复执行同样的三条", 0);

        counter.reset();
        try (SqlSession session = factory.openSession()) {
            session.getMapper(PhotographerCacheMapper.class).findByStudio("婚礼纪实组");
        }
        check(counter, "换成另一个工作室参数", 1);
        System.out.println("    当前缓存中的条目数: " + factory.getConfiguration().getCache(NS).getSize());
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    /** 预热：用一个会关闭的 Session 把数据提交进二级缓存 */
    private static Photographer warmUp(SqlSessionFactory factory, int photographerId) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(PhotographerCacheMapper.class).findByPrimaryKey(photographerId);
        }
    }

    private static String describeCache(SqlSessionFactory factory) {
        Cache cache = factory.getConfiguration().getCache(NS);
        return cache == null ? "未开启" : cache.getClass().getName() + "（MyBatis 会自动包一层 LoggingCache）";
    }

    private static void check(SqlAccessCounter counter, String title, int expected) {
        int actual = counter.sqlCount();
        System.out.printf("  · %-52s 实际 SQL %d 次 / 期望 %d 次  %s%n",
                title, actual, expected, actual == expected ? "✔" : "✘ 与预期不符");
    }

    private static void banner(String title) {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println(title);
        System.out.println("==============================================================");
    }
}
