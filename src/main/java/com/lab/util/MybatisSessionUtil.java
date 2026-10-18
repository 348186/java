package com.lab.util;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 会话工厂工具类。
 * SqlSessionFactory 全局唯一、建造代价高，使用静态代码块在类加载时构建一次；
 * SqlSession 非线程安全，每次使用完必须关闭（推荐 try-with-resources）。
 */
public final class MybatisSessionUtil {

    private static final String CONFIG = "mybatis-config.xml";

    private static final SqlSessionFactory FACTORY;

    static {
        try (InputStream in = Resources.getResourceAsStream(CONFIG)) {
            // 使用 MP 提供的 Builder，才能为 BaseMapper 注入通用 CRUD 语句
            FACTORY = new MybatisSqlSessionFactoryBuilder().build(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("加载 " + CONFIG + " 失败：" + e.getMessage());
        }
    }

    private MybatisSessionUtil() {
    }

    /** 获取全局唯一的会话工厂 */
    public static SqlSessionFactory getFactory() {
        return FACTORY;
    }

    /** 开启手工提交事务的会话 */
    public static SqlSession openSession() {
        return FACTORY.openSession();
    }

    /** 开启自动提交的会话（仅查询或单条写操作场景） */
    public static SqlSession openSession(boolean autoCommit) {
        return FACTORY.openSession(autoCommit);
    }
}
