package com.lab.plugin;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;

/**
 * MyBatis-Plus 拦截器宿主（供 mybatis-config.xml 的 &lt;plugin&gt; 标签使用）。
 *
 * <p>设计原因：MyBatis 通过 &lt;plugin interceptor="..."/&gt; 注册插件时只会调用无参构造，
 * 没有机会再调用 addInnerInterceptor()；写成子类并在构造器中完成装配，
 * 就能让「原生 MyBatis 启动方式」同样具备 MP 的分页能力。
 *
 * <p>坑点：{@code @Intercepts} 注解不会被子类继承，且 MP 的分页逻辑依赖
 * Executor#query 签名（PaginationInnerInterceptor#willDoQuery 由此触发），
 * 因此子类必须完整重新声明 MP 父类上的全部签名，否则分页静默失效（total 恒为 0）。
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare",
                args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})
})
public class MpPaginationPlugin extends MybatisPlusInterceptor {

    public MpPaginationPlugin() {
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(500L);
        pagination.setOverflow(false);
        addInnerInterceptor(pagination);
    }
}
