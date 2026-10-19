package com.lab.cache;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

/**
 * SQL 执行次数计数器。
 *
 * <p>原理：MyBatis 通过 SLF4J（底层 Logback）以 {@code namespace.statementId} 为 logger 名打印
 * {@code ==>  Preparing: SELECT ...}，因此只要在指定 logger 上挂一个内存型 Appender，
 * 统计包含 {@code "==>  Preparing"} 的日志条数，就能<strong>自动判定一段代码究竟发了几次 SQL</strong>，
 * 不必靠肉眼看控制台。
 *
 * <p>用法：
 * <pre>{@code
 * try (SqlAccessCounter counter = new SqlAccessCounter("com.lab.cache")) {
 *     counter.reset();
 *     mapper.findByPrimaryKey(1);
 *     System.out.println(counter.sqlCount());  // 1
 * }
 * }</pre>
 */
public final class SqlAccessCounter implements AutoCloseable {

    /** MyBatis 打印 SQL 时的固定前缀，注意 Preparing 前有两个空格 */
    private static final String PREPARING_FLAG = "==>  Preparing:";

    private final Logger target;

    private final ListAppender<ILoggingEvent> appender;

    private final Level originLevel;

    public SqlAccessCounter(String loggerName) {
        this.target = (Logger) LoggerFactory.getLogger(loggerName);
        this.originLevel = target.getLevel();
        this.appender = new ListAppender<>();
        this.appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        this.appender.start();
        target.addAppender(appender);
        // 缓存命中与否依赖 DEBUG 级别日志，这里强制打开，退出时还原
        if (!target.isDebugEnabled()) {
            target.setLevel(Level.DEBUG);
        }
    }

    /** 本次统计区间内实际发送到数据库的 SQL 条数 */
    public int sqlCount() {
        int n = 0;
        for (ILoggingEvent event : appender.list) {
            if (event.getFormattedMessage().contains(PREPARING_FLAG)) {
                n++;
            }
        }
        return n;
    }

    /** 取本区间内的 Cache Hit Ratio 日志（若有），便于观察二级缓存命中率 */
    public String lastHitRatio() {
        String last = null;
        for (ILoggingEvent event : appender.list) {
            String msg = event.getFormattedMessage();
            if (msg.contains("Cache Hit Ratio")) {
                last = msg.substring(msg.indexOf("Cache Hit Ratio"));
            }
        }
        return last == null ? "（本次无二级缓存访问）" : last;
    }

    /** 清空统计区间 */
    public void reset() {
        appender.list.clear();
    }

    @Override
    public void close() {
        target.detachAppender(appender);
        appender.stop();
        target.setLevel(originLevel);
    }
}
