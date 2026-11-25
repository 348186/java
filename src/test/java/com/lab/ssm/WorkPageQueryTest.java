package com.lab.ssm;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.PhotoWorkPlus;
import com.lab.mapper.PhotoWorkPlusMapper;
import com.lab.service.impl.WorkServiceImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 步骤 5.1：Service 层分页多条件查询测试。
 * 不启动 Spring 容器（避免为测试再引 spring-test），直接手工构建
 * Druid + MybatisConfiguration + 分页插件，再注入 WorkServiceImpl，验证查询条件拼装是否正确。
 */
class WorkPageQueryTest {

    static {
        // Druid 建连日志太多，测试期间关掉，只保留业务输出
        Logger.getLogger("com.alibaba.druid").setLevel(Level.OFF);
    }

    private DruidDataSource dataSource;
    private SqlSession sqlSession;
    private WorkServiceImpl workService;

    @BeforeEach
    void setUp() throws Exception {
        Properties props = new Properties();
        try (var in = getClass().getClassLoader().getResourceAsStream("jdbc.properties")) {
            props.load(in);
        }
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(props.getProperty("jdbc.driver"));
        dataSource.setUrl(props.getProperty("jdbc.url"));
        dataSource.setUsername(props.getProperty("jdbc.username"));
        dataSource.setPassword(props.getProperty("jdbc.password"));

        Environment env = new Environment("test", new JdbcTransactionFactory(), dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(env);
        configuration.setMapUnderscoreToCamelCase(true);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(PhotoWorkPlusMapper.class);

        SqlSessionFactory factory = new DefaultSqlSessionFactory(configuration);
        sqlSession = factory.openSession(true);
        PhotoWorkPlusMapper mapper = sqlSession.getMapper(PhotoWorkPlusMapper.class);
        workService = new WorkServiceImpl() {
            {
                this.baseMapper = mapper;
            }
        };
    }

    @AfterEach
    void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void printPage(String title, Page<PhotoWorkPlus> page) {
        System.out.println("=== " + title + " ===");
        System.out.println("total=" + page.getTotal()
                + ", pages=" + page.getPages()
                + ", records=" + page.getRecords().size());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (PhotoWorkPlus work : page.getRecords()) {
            System.out.printf("id=%d, title=%s, style=%s, price=%s, photographerId=%d, shootDate=%s%n",
                    work.getWorkId(),
                    work.getWorkTitle(),
                    work.getStyle(),
                    work.getPrice(),
                    work.getPhotographerId(),
                    work.getShootDate() == null ? "-" : format.format(work.getShootDate()));
        }
    }

    @Test
    void pageQueryWithoutCondition_pagination() {
        Page<PhotoWorkPlus> page = workService.pageQuery(1, 5, null);
        printPage("无条件分页：page=1,size=5", page);
        assertEquals(23, page.getTotal());
        assertEquals(5, page.getPages());
        assertEquals(5, page.getRecords().size());
    }

    @Test
    void pageQueryByStyle_eq() {
        WorkQueryDTO query = new WorkQueryDTO();
        query.setStyle("人像");
        Page<PhotoWorkPlus> page = workService.pageQuery(1, 5, query);
        printPage("风格精确查询：style=人像", page);
        assertEquals(3, page.getTotal());
        assertTrue(page.getRecords().stream().allMatch(w -> "人像".equals(w.getStyle())));
    }

    @Test
    void pageQueryByTitle_like() {
        WorkQueryDTO query = new WorkQueryDTO();
        query.setWorkTitle("城市");
        Page<PhotoWorkPlus> page = workService.pageQuery(1, 5, query);
        printPage("标题模糊查询：workTitle=城市", page);
        assertEquals(1, page.getTotal());
        assertTrue(page.getRecords().get(0).getWorkTitle().contains("城市"));
    }

    @Test
    void pageQueryByPriceRange_andPhotographer() {
        WorkQueryDTO query = new WorkQueryDTO();
        query.setPhotographerId(1);
        query.setMinPrice(new java.math.BigDecimal("1000"));
        Page<PhotoWorkPlus> page = workService.pageQuery(1, 5, query);
        printPage("多条件查询：photographerId=1, price>=1000", page);
        assertEquals(2, page.getTotal());
        assertTrue(page.getRecords().stream()
                .allMatch(w -> w.getPhotographerId() == 1
                        && w.getPrice().compareTo(new java.math.BigDecimal("1000")) >= 0));
    }

    @Test
    void pageQueryBeyondLastPage_emptyRecords() {
        Page<PhotoWorkPlus> page = workService.pageQuery(99, 5, null);
        printPage("超尾页分页：page=99,size=5", page);
        assertEquals(23, page.getTotal());
        assertEquals(0, page.getRecords().size());
    }

    @Test
    void pageQueryIllegalParams_fallbackToDefault() {
        Page<PhotoWorkPlus> page = workService.pageQuery(0, 0, null);
        printPage("非法分页参数回退：page=0,size=0", page);
        assertEquals(1, page.getCurrent());
        assertEquals(5, page.getSize());
        assertEquals(5, page.getRecords().size());
    }
}
