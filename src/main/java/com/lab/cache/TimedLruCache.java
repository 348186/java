package com.lab.cache;

import org.apache.ibatis.cache.Cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 自定义二级缓存实现：<strong>带过期时间（TTL）的 LRU 缓存</strong>。
 *
 * <p>为什么要自己写，而不是直接用 MyBatis 内置的 LRU？
 * <ol>
 *   <li>内置的 {@code LruCache} + {@code PerpetualCache} 只有「容量淘汰」，没有时间维度，
 *       数据一旦进来就永远不会过期。给缓存加上 TTL 是生产环境的常见诉求。</li>
 *   <li>MyBatis 对自定义 Cache 的处理与内置缓存<strong>不同</strong>：
 *       {@code CacheBuilder#build()} 只会对 {@code PerpetualCache} 追加
 *       LRU / Serialized / Synchronized 等装饰器，自定义实现只会被包一层 {@code LoggingCache}。
 *       所以——
 *       <ul>
 *         <li>线程安全要自己保证（这里用 {@code ReentrantReadWriteLock}）；</li>
 *         <li>返回拷贝要自己做（这里在 put 时把值序列化成字节、get 时反序列化，
 *             从而实现「不同 SqlSession 拿到的是不同对象实例」的效果，与内置行为一致）。</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>使用方式见 {@code com/lab/cache/EmpCacheMapper.xml} 中的 {@code <cache type="...">}。
 * 其中 {@code size} / {@code ttlSeconds} 两个属性由 MyBatis 依据 {@code <property>} 注入，
 * 因此必须提供对应的 public setter，且必须提供 {@code (String id)} 构造器供 MyBatis 反射实例化。
 */
public class TimedLruCache implements Cache {

    /** 缓存 namespace（MyBatis 传入，用于区分不同 Mapper 的缓存实例） */
    private final String id;

    /** accessOrder = true 的 LinkedHashMap：最近访问的元素会被移到链表尾部 */
    private final Map<Object, Node> store = new LinkedHashMap<>(64, 0.75f, true);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /** 最多缓存多少条，超过则淘汰最久未访问的 */
    private volatile int size = 512;

    /** 条目存活时长（毫秒），超过则该条目视为失效 */
    private volatile long ttlMillis = 60_000L;

    private long hits;

    private long misses;

    public TimedLruCache(String id) {
        this.id = id;
    }

    // ---------------- 以下是供 MyBatis <property> / @Property 注入的配置项 ----------------

    public void setSize(int size) {
        this.size = size > 0 ? size : 1;
    }

    public void setTtlSeconds(long seconds) {
        this.ttlMillis = seconds > 0 ? seconds * 1000L : Long.MAX_VALUE;
    }

    // ---------------- Cache 接口实现 ----------------

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSize() {
        rwLock.readLock().lock();
        try {
            return store.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void putObject(Object key, Object value) {
        rwLock.writeLock().lock();
        try {
            expirePass();
            store.put(key, new Node(serialize(value)));
            // 容量淘汰：逐出最久未访问的头部节点
            Iterator<Object> it = store.keySet().iterator();
            while (store.size() > size && it.hasNext()) {
                it.next();
                it.remove();
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Object getObject(Object key) {
        // 注意：accessOrder=true 时 get 也会改动链表结构，因此这里用写锁而非读锁
        rwLock.writeLock().lock();
        try {
            Node node = store.get(key);
            if (node == null) {
                misses++;
                return null;
            }
            if (node.isExpired(ttlMillis)) {
                store.remove(key);
                misses++;
                return null;
            }
            hits++;
            return deserialize(node.payload);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Object removeObject(Object key) {
        rwLock.writeLock().lock();
        try {
            Node node = store.remove(key);
            return node == null ? null : deserialize(node.payload);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            store.clear();
            hits = 0;
            misses = 0;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return rwLock;
    }

    // ---------------- 便于观察的自统计信息 ----------------

    /** 命中率 = 命中次数 / 总查询次数 */
    public double getHitRatio() {
        rwLock.readLock().lock();
        try {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /** 输出当前缓存的运行状态，用于演示与排障 */
    public String stats() {
        rwLock.readLock().lock();
        try {
            long total = hits + misses;
            return String.format("%s [size=%d/%d, hit=%d, miss=%d, ratio=%.2f, ttl=%dms]",
                    id, store.size(), size, hits, misses,
                    total == 0 ? 0.0 : (double) hits / total, ttlMillis);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // ---------------- 内部实现 ----------------

    /** 惰性删除：每次增删改出发地清理一遍过期条目 */
    private void expirePass() {
        Iterator<Map.Entry<Object, Node>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isExpired(ttlMillis)) {
                it.remove();
            }
        }
    }

    /**
     * 把缓存值序列化成字节，保证「取出来的一定是副本」。
     * 若遇到不可序列化的对象，退化为直接保存引用并打印警告，避免整个查询失败。
     */
    private byte[] serialize(Object value) {
        if (value == null) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException | RuntimeException e) {
            System.err.println("[" + id + "] 序列化缓存值失败，退化为直接保存引用：" + e.getMessage());
            return null;
        }
    }

    private Object deserialize(byte[] payload) {
        if (payload == null) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        } catch (Exception e) {
            System.err.println("[" + id + "] 反序列化缓存值失败，按未命中处理：" + e.getMessage());
            return null;
        }
    }

    /** 缓存条目：值 + 写入时刻 */
    private static class Node {

        private final byte[] payload;

        private final long createdAt = System.currentTimeMillis();

        Node(byte[] payload) {
            this.payload = payload;
        }

        boolean isExpired(long ttl) {
            return ttl == Long.MAX_VALUE ? false : System.currentTimeMillis() - createdAt > ttl;
        }
    }

    // MyBatis 的 LoggingCache 等装饰器按 id 比较缓存实例，遵循其约定实现
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimedLruCache other = (TimedLruCache) o;
        return java.util.Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TimedLruCache(" + stats() + ')';
    }
}
