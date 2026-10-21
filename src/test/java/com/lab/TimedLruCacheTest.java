package com.lab;

import com.lab.cache.TimedLruCache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 实验三 · 自定义二级缓存 {@link TimedLruCache} 的纯单元测试（不连数据库）。
 * 单独 isolating 出来测，是为了验证它与 MyBatis 无关的自身行为：
 * LRU 淘汰、TTL 过期、命中率统计、拷贝语义。
 */
public class TimedLruCacheTest {

    private static final String ID = "com.lab.cache.PhotographerCacheMapper";

    @Test
    // MyBatis 约定：必须有 (String id) 构造器，getId 返回该 id
    public void createdByMyBatisConvention() {
        TimedLruCache cache = new TimedLruCache(ID);
        assertEquals(ID, cache.getId());
        assertEquals(0, cache.getSize());
    }

    @Test
    // 存入后可读出，并且读出的是序列化副本（不同实例）
    public void valueRoundTripAndCopySemantics() {
        TimedLruCache cache = new TimedLruCache(ID);
        String value = "婚礼纪实组";

        cache.putObject("k1", value);
        Object restored = cache.getObject("k1");

        assertEquals(value, restored);
        assertEquals(1, cache.getSize());
        // 字符串常量这里会被常量池复用，重点验证 HashMap 这类可变容器确实是新对象
        cache.putObject("k2", new java.util.ArrayList<>(java.util.Arrays.asList("a", "b")));
        Object list1 = cache.getObject("k2");
        Object list2 = cache.getObject("k2");
        assertNotSame("每次取出都应是反序列化产生的新副本", list1, list2);
    }

    @Test
    // 超过 size 时淘汰最久未访问的条目（LRU）
    public void evictLeastRecentlyUsed() {
        TimedLruCache cache = new TimedLruCache(ID);
        cache.setSize(2);

        cache.putObject("k1", "v1");
        cache.putObject("k2", "v2");
        cache.getObject("k1");          // 访问后 k1 变为最近使用
        cache.putObject("k3", "v3");    // 容量超限，淘汰最久未用的 k2

        assertNotNull(cache.getObject("k1"));
        assertNull("k2 最久未用，应被淘汰", cache.getObject("k2"));
        assertEquals("v3", cache.getObject("k3"));
        assertEquals(2, cache.getSize());
    }

    @Test
    // 超过 TTL 的条目自动失效
    public void expireByTtl() throws InterruptedException {
        TimedLruCache cache = new TimedLruCache(ID);
        cache.setTtlSeconds(1L);

        cache.putObject("k1", "v1");
        assertEquals("v1", cache.getObject("k1"));

        Thread.sleep(1100);
        assertNull("TTL 到期后应按未命中处理", cache.getObject("k1"));
    }

    @Test
    // clear() 清空数据并重置命中率统计
    public void clearResetsEverything() {
        TimedLruCache cache = new TimedLruCache(ID);
        cache.putObject("k1", "v1");
        cache.getObject("k1");
        cache.getObject("missing");
        assertTrue(cache.getHitRatio() > 0);

        cache.clear();
        assertEquals(0, cache.getSize());
        assertEquals(0.0, cache.getHitRatio(), 1e-9);
        assertNull(cache.getObject("k1"));
    }

    @Test
    // 命中率统计 = 命中次数 / (命中 + 未命中)
    public void hitRatioStatistic() {
        TimedLruCache cache = new TimedLruCache(ID);
        cache.putObject("k1", "v1");

        cache.getObject("k1");     // hit
        cache.getObject("k2");     // miss
        cache.getObject("k3");     // miss

        assertEquals(1.0 / 3, cache.getHitRatio(), 1e-9);
        assertTrue("stats 输出应包含命中率", cache.stats().contains("ratio=0.33"));
    }
}
