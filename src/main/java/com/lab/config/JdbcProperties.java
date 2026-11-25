package com.lab.config;

/**
 * 数据库连接参数载体。
 * 纯 Spring 工程没有 Spring Boot 的 ConfigurationProperties 绑定器，
 * 因此由 SpringConfig 用 @Value 读取 jdbc.properties 后构造该 record。
 */
public record JdbcProperties(String driver, String url, String username, String password) {
}
