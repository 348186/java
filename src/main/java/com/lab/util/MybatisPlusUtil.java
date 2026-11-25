package com.lab.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lab.mapper.PhotoWorkPlusMapper;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;

/**
 * 纯 Java 环境（无 Spring）下构建 MyBatis-Plus 会话工厂与分页插件。
 */
public final class MybatisPlusUtil {
    private static final SqlSessionFactory FACTORY;

    static {
        UnpooledDataSource dataSource = new UnpooledDataSource(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/photo_studio?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                "root", "348186");

        Environment env = new Environment("dev", new JdbcTransactionFactory(), dataSource);

        MybatisConfiguration config = new MybatisConfiguration();
        config.setEnvironment(env);
        config.setMapUnderscoreToCamelCase(true);
        config.setLogImpl(Log4j2Impl.class);

        // 分页插件：MySQL 方言
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);

        // 注册 Mapper（MP 会注入 BaseMapper 的通用方法）
        config.addMapper(PhotoWorkPlusMapper.class);

        FACTORY = new MybatisSqlSessionFactoryBuilder().build(config);
    }

    private MybatisPlusUtil() {
    }

    public static SqlSession openSession() {
        return FACTORY.openSession();
    }
}
