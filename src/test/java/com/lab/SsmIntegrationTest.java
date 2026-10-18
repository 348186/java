package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lab.emp.config.RootAppConfig;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.service.EmpService;
import com.lab.entity.Emp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实验二 · SSM 整合冒烟测试（不依赖 Tomcat）。
 * 直接启动 Spring 根容器，验证数据源、Mapper 扫描、事务管理器与 Service 装配是否正确。
 */
class SsmIntegrationTest {

    @Test
    @DisplayName("步骤3.4：Spring 根容器可正常启动并完成 Bean 装配")
    void testContainerStartup() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(RootAppConfig.class)) {

            EmpService empService = ctx.getBean(EmpService.class);
            assertNotNull(empService, "EmpService 应被扫描并装配");
            assertNotNull(ctx.getBean(javax.sql.DataSource.class), "数据源应可用");
            assertNotNull(ctx.getBean(org.springframework.transaction.PlatformTransactionManager.class),
                    "事务管理器应可用");

            // 验证数据库连通（与实验一同一张 emp 表）
            assertTrue(empService.count() >= 4, "emp 表至少应包含指导书给定的 4 条数据");
            System.out.println("员工总数 = " + empService.count());
        }
    }

    @Test
    @DisplayName("步骤5.1：Service 层分页多条件查询")
    void testPageQuery() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(RootAppConfig.class)) {
            EmpService empService = ctx.getBean(EmpService.class);

            EmpQueryDTO query = new EmpQueryDTO();
            query.setDept("研发部");
            long expected = empService.count(new LambdaQueryWrapper<Emp>().eq(Emp::getDept, "研发部"));
            IPage<Emp> page = empService.searchByPage(1, 2, query);
            assertEquals(expected, page.getTotal(), "分页总数应与条件查询结果一致");
            assertTrue(page.getRecords().size() <= 2, "每页最多 2 条");
            page.getRecords().forEach(System.out::println);

            // 分页参数非法时自动兜底，不应抛异常
            assertDoesNotThrow(() -> empService.searchByPage(0, -1, null));
        }
    }

    @Test
    @DisplayName("步骤6.5：声明式事务生效 —— 抛异常后数据回滚")
    void testTransactionalRollback() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(RootAppConfig.class)) {
            EmpService empService = ctx.getBean(EmpService.class);
            long before = empService.count();

            Emp emp = new Emp("事务-回滚测试", "男", "测试部", "临时岗",
                    new BigDecimal("6000"), java.time.LocalDate.of(2025, 5, 1), 1);
            try {
                empService.saveAndFail(emp);
            } catch (RuntimeException ignored) {
                // 预期异常
            }
            assertEquals(before, empService.count(), "事务回滚后记录数应保持不变");
            assertTrue(empService.list().stream().noneMatch(e -> "事务-回滚测试".equals(e.getEmpName())));
        }
    }
}
