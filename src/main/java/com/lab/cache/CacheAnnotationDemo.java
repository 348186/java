package com.lab.cache;

import com.lab.cache.annotation.EmpCacheAnnotationMapper;
import com.lab.entity.Emp;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.math.BigDecimal;

/**
 * 实验三 · MyBatis 缓存机制 —— 注解方式（{@code @CacheNamespace}）演示入口。
 *
 * <p>验证内容与 {@link CacheXmlDemo} 完全一致，用来说明：
 * 「在 Mapper XML 里写 {@code <cache/>}」与「在接口上标 {@code @CacheNamespace}」
 * 是等价的两种开启方式，区别只在配置载体；二者不可用于同一个 namespace。
 */
public class CacheAnnotationDemo {

    private static final String NS = EmpCacheAnnotationMapper.class.getName();

    public static void main(String[] args) {
        SqlSessionFactory factory = CacheSessionFactory.newAnnotationFactory();

        try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
            System.out.println("==============================================================");
            System.out.println("注解方式：@CacheNamespace(implementation = TimedLruCache.class)");
            System.out.println("==============================================================");
            System.out.println("    当前 namespace 的二级缓存: "
                    + factory.getConfiguration().getCache(NS).getClass().getName());

            Emp fromSession1;
            try (SqlSession session = factory.openSession()) {
                fromSession1 = session.getMapper(EmpCacheAnnotationMapper.class).findByPrimaryKey(1);
            }
            System.out.println("    Session1 查询 -> " + fromSession1.getEmpName() + " / " + fromSession1.getSalary());

            Emp fromSession2;
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                fromSession2 = session.getMapper(EmpCacheAnnotationMapper.class).findByPrimaryKey(1);
            }
            report(counter, "Session2 查询相同数据（跨 Session 命中二级缓存）", 0);
            System.out.println("    是否同一对象引用: " + (fromSession1 == fromSession2) + "  → 副本，非同一对象");

            System.out.println("\n  【useCache=false 的 select】");
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheAnnotationMapper.class).findAll();
            }
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheAnnotationMapper.class).findAll();
            }
            report(counter, "两个 Session 各执行一次 findAll（@Options(useCache = false)）", 2);

            System.out.println("\n  【写操作清空二级缓存】");
            BigDecimal origin;
            try (SqlSession session = factory.openSession()) {
                EmpCacheAnnotationMapper mapper = session.getMapper(EmpCacheAnnotationMapper.class);
                origin = mapper.findByPrimaryKey(1).getSalary();
                mapper.renewSalary(1, origin);
                session.commit();
            }
            counter.reset();
            try (SqlSession session = factory.openSession()) {
                session.getMapper(EmpCacheAnnotationMapper.class).findByPrimaryKey(1);
            }
            report(counter, "写回原值并提交后再次查询", 1);
        }
    }

    private static void report(SqlAccessCounter counter, String title, int expected) {
        int actual = counter.sqlCount();
        System.out.printf("  · %-52s 实际 SQL %d 次 / 期望 %d 次  %s%n",
                title, actual, expected, actual == expected ? "✔" : "✘ 与预期不符");
    }
}
