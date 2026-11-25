package com.lab.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.annotation.DbType;
import com.lab.mapper.PhotoWorkPlusMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 根容器：只负责数据源、MyBatis-Plus、事务与 Service 层，不接触 Web 组件。
 */
@Configuration
@ComponentScan(basePackages = "com.lab.service")
@PropertySource("classpath:jdbc.properties")
@EnableTransactionManagement
public class SpringConfig {

    @Bean
    public JdbcProperties jdbcProperties(
            @Value("${jdbc.driver}") String driver,
            @Value("${jdbc.url}") String url,
            @Value("${jdbc.username}") String username,
            @Value("${jdbc.password}") String password) {
        return new JdbcProperties(driver, url, username, password);
    }

    @Bean
    public DataSource dataSource(JdbcProperties properties) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(properties.driver());
        ds.setUrl(properties.url());
        ds.setUsername(properties.username());
        ds.setPassword(properties.password());
        ds.setInitialSize(5);
        ds.setMaxActive(20);
        return ds;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                               MybatisPlusInterceptor interceptor) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTypeAliasesPackage("com.lab.entity");
        factory.setPlugins(interceptor);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        return factory.getObject();
    }

    /**
     * 只把已存在的 PhotoWorkPlusMapper 交给 Spring 管理，避免改动原 MyBatis 文件。
     */
    @Bean
    public MapperFactoryBean<PhotoWorkPlusMapper> photoWorkPlusMapper(SqlSessionFactory factory) {
        MapperFactoryBean<PhotoWorkPlusMapper> mapper =
                new MapperFactoryBean<>(PhotoWorkPlusMapper.class);
        mapper.setSqlSessionFactory(factory);
        return mapper;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
