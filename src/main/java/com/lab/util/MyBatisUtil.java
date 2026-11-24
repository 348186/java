package com.lab.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * MyBatis 会话工厂工具类：工厂全局唯一，SqlSession 每次从工厂获取并关闭。
 */
public final class MyBatisUtil {
    private static final SqlSessionFactory FACTORY;

    static {
        try {
            InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
            FACTORY = new SqlSessionFactoryBuilder().build(in);
            in.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("初始化 MyBatis 失败: " + e.getMessage());
        }
    }

    private MyBatisUtil() {
    }

    public static SqlSessionFactory getFactory() {
        return FACTORY;
    }

    /** 开启会话，默认不自动提交（增删改须手动 commit） */
    public static SqlSession openSession() {
        return FACTORY.openSession();
    }
}
