package com.lab.cache;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 缓存实验的 SqlSessionFactory 工厂。
 *
 * <p>与实验一的 {@link com.lab.util.MybatisSessionUtil} 的区别：
 * {@code MybatisSessionUtil} 是全局单例（复用同一个 {@code Configuration}，
 * 因而也复用同一个二级缓存实例）；这里每次调用 {@code newXxx()} 都会
 * <strong>新建一个 SessionFactory</strong>，得到一份“干净”的二级缓存，
 * 避免各组缓存实验之间互相干扰。
 */
public final class CacheSessionFactory {

    /** localCacheScope = SESSION + 自定义 TimedLruCache 二级缓存 */
    public static final String XML_CONFIG = "cache/xml/mybatis-config.xml";

    /**
     * 关闭二级缓存（cacheEnabled=false）的专用配置：
     * 二级缓存一旦开启，commit/close 会把查询结果写进去，
     * 「commit 后一级缓存失效」这类实验就可能被二级缓存命中掩盖，
     * 因此纯一级缓存实验统一使用这套配置。
     */
    public static final String LEVEL_ONE_CONFIG = "cache/l1-only/mybatis-config.xml";

    /** localCacheScope = STATEMENT（一级缓存对照实验） */
    public static final String STATEMENT_CONFIG = "cache/statement/mybatis-config.xml";

    /** 注解方式：@CacheNamespace */
    public static final String ANNOTATION_CONFIG = "cache/annotation/mybatis-config.xml";

    private CacheSessionFactory() {
    }

    public static SqlSessionFactory newXmlFactory() {
        return build(XML_CONFIG);
    }

    /** 仅开启一级缓存的全新工厂 */
    public static SqlSessionFactory newLevelOneFactory() {
        return build(LEVEL_ONE_CONFIG);
    }

    public static SqlSessionFactory newStatementScopeFactory() {
        return build(STATEMENT_CONFIG);
    }

    public static SqlSessionFactory newAnnotationFactory() {
        return build(ANNOTATION_CONFIG);
    }

    public static SqlSessionFactory build(String resource) {
        try (InputStream in = Resources.getResourceAsStream(resource)) {
            return new SqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            throw new IllegalStateException("无法加载 MyBatis 配置文件：" + resource, e);
        }
    }
}
