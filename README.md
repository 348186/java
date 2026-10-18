# EMP 员工管理 MIS 实验工程（MyBatis + MyBatis-Plus + SSM）

对应《JAVA框架技术（一）》（AI 赋能版）课程实验指导书：

- **实验一**：AI 赋能 MyBatis 与 MyBatis-Plus 环境搭建及 CRUD 实操
- **实验二**：AI 赋能 SSM 框架整合与 EMP 员工管理 MIS 系统开发

> 工程功能点严格按指导书要求实现；**依赖版本、类名、方法名、配置写法均与指导书示例不同**（独立实现），
> 但**数据库库名、表结构、初始数据与指导书完全一致**（`ssm_emp.emp` 的 4 条数据）。

---

## 一、环境要求

| 类别 | 版本 |
|---|---|
| JDK | 11（当前机器为 JDK 25，编译参数 `--release 11`） |
| Maven | 3.8.x + 阿里云镜像 |
| MySQL | 8.0 / 9.x 均可 |
| Tomcat | 9.x（javax.servlet 体系） |

## 二、数据库初始化

```bash
mysql -uroot -p < sql/init.sql
```

库名 `ssm_emp`，表 `emp`，共 14 条数据。
前 4 条与指导书「步骤 1.1」逐字一致，第 5—14 条为自主扩充（部门、岗位、薪资风格保持一致）：

| emp_id | emp_name | gender | dept | post | salary | hire_date | status | 来源 |
|---|---|---|---|---|---|---|---|---|
| 1 | 张伟 | 男 | 研发部 | Java工程师 | 12000.00 | 2024-07-01 | 1 | 指导书 |
| 2 | 李娜 | 女 | 研发部 | 前端工程师 | 10000.00 | 2024-08-15 | 1 | 指导书 |
| 3 | 王强 | 男 | 市场部 | 市场专员 | 8000.00 | 2023-03-10 | 1 | 指导书 |
| 4 | 赵敏 | 女 | 人事部 | 人事专员 | 7500.00 | 2022-06-20 | 0 | 指导书 |
| 5 | 张三 | 男 | 研发部 | 后端工程师 | 13500.00 | 2023-05-08 | 1 | 扩充 |
| 6 | 李四 | 女 | 市场部 | 市场经理 | 15000.00 | 2021-09-12 | 1 | 扩充 |
| 7 | 刘岩 | 男 | 财务部 | 会计 | 9500.00 | 2022-11-20 | 1 | 扩充 |
| 8 | 权小乐 | 女 | 人事部 | 招聘专员 | 8200.00 | 2024-03-04 | 1 | 扩充 |
| 9 | 孙浩 | 男 | 研发部 | 前端工程师 | 11500.00 | 2023-08-16 | 1 | 扩充 |
| 10 | 周雨欣 | 女 | 测试部 | 测试工程师 | 9800.00 | 2024-01-09 | 1 | 扩充 |
| 11 | 吴俊杰 | 男 | 运维部 | 运维工程师 | 10500.00 | 2022-04-25 | 1 | 扩充 |
| 12 | 郑晓彤 | 女 | 研发部 | 数据工程师 | 12800.00 | 2025-02-18 | 1 | 扩充 |
| 13 | 冯磊 | 男 | 市场部 | 销售代表 | 7600.00 | 2021-06-30 | 0 | 扩充 |
| 14 | 何静怡 | 女 | 财务部 | 出纳 | 7200.00 | 2023-10-11 | 0 | 扩充 |

> 单元测试不写死行数，而是与 MP 的 `selectCount()` 交叉校验，
> 因此后续继续增删员工数据时测试仍然有效。

连接四要素在 `src/main/resources/jdbc.properties` 中修改（**禁止硬编码到 Java 代码**）。

## 三、与指导书的差异化对照

### 3.1 依赖版本（逐项不同）

| 依赖 | 指导书版本 | 本工程版本 |
|---|---|---|
| spring-webmvc / spring-jdbc / spring-aspects | 5.3.31 | **5.3.34** |
| mybatis-plus | 3.5.3.1 | **3.5.5** |
| mybatis-spring | 未声明 | **2.1.2**（显式声明） |
| druid | 1.2.20 | **1.2.21** |
| mysql-connector-j | 8.0.33 | **8.4.0** |
| jackson-databind | 2.15.3 | **2.18.0**（另加 jsr310 模块） |
| javax.servlet-api | 4.0.1 | **3.1.0** |
| hibernate-validator | 6.2.5.Final | **6.2.3.Final** |
| 日志 | log4j 1.2.17 | **slf4j 1.7.36 + logback 1.2.13** |
| 单元测试 | JUnit 4.13.2 | **JUnit 5.10.2** |
| aspectjweaver | 未声明 | **1.9.21** |

### 3.2 代码层面的区分

| 指导书示例 | 本工程实现 |
|---|---|
| `MyBatisUtil` | `MybatisSessionUtil`（静态块构建工厂，含 `openSession(autoCommit)` 重载） |
| `EmpMapper.selectById/insert/update/deleteById` | `findByPrimaryKey / addEmp / modifyEmp / removeEmp`（避免与 `BaseMapper` 重载歧义） |
| `selectByCondition / updateDynamic / deleteBatch / insertBatch` | `queryByExample / modifySelective / removeInBatch / addInBatch` |
| 分页插件在 XML 中写 `<property name="@...PaginationInnerInterceptor"/>`（MP 3.4 旧写法） | 自定义 `MpPaginationPlugin` 子类，构造器内装配 `PaginationInnerInterceptor(DbType.MYSQL)` |
| `SpringConfig` | `RootAppConfig`（额外注入连接池参数、防全表更新拦截器、排除 `WebMvcConfig`） |
| `SpringMvcConfig` + `ServletContainersInitConfig` | `WebMvcConfig` + `WebInitializer` |
| `Result` 使用裸 int 状态码 | `ResultCode` 枚举 + `Result<T>`（含 `timestamp`） |
| `EmpQueryDTO` 无薪资筛选 | 增加 `minSalary`、`status` 条件 |
| `Emp.hireDate` 用 `java.util.Date` | 用 `java.time.LocalDate`（JDK8 时间 API） |
| `Emp.status` 标注 `@TableLogic` | 不启用逻辑删除（避免把「离职」误当成「已删除」而查不到赵敏） |
| `WebLogAspect` | `RequestLogAspect`（记录 URI、耗时，使用 SLF4J） |
| 拦截器中手写 JSON 字符串 | 用 Jackson `ObjectMapper` 序列化 `Result`，并配套 `AuthController` |
| Mapper 扫描用 `MapperScannerConfigurer` | 使用 `@MapperScan` |
| `EmpMapper.xml` 放 `resources/mapper/` | 放 `resources/com/lab/mapper/`，支持 `<package>` 包扫描 |

### 3.3 数据库保持一致

`sql/init.sql` 中的库名、表名、字段、类型、默认值、注释以及指导书给出的 4 条 INSERT 数据，
与指导书「步骤 1.1 数据库准备」完全一致；在此基础上扩充了 10 条同类风格的数据
（张三、李四、刘岩、权小乐、孙浩、周雨欣、吴俊杰、郑晓彤、冯磊、何静怡），
并按任务 4.1 要求补充了 `idx_emp_name`、`idx_emp_dept` 索引与非空/默认值约束。

## 四、目录结构

```
src/main/java/com/lab
├── entity/Emp.java                  # 实体（MP 注解 + JSR-303 校验）
├── mapper/EmpMapper.java            # 继承 BaseMapper + 自定义动态 SQL 方法
├── util/MybatisSessionUtil.java     # 实验一会话工具类
├── plugin/MpPaginationPlugin.java   # 分页插件宿主
├── runner/LabRunner.java            # 实验一 main 入口
└── emp/                             # 实验二 SSM
    ├── config/{WebInitializer, RootAppConfig, WebMvcConfig}
    ├── controller/{EmpController, AuthController}
    ├── service/{EmpService, impl/EmpServiceImpl}
    ├── dto/EmpQueryDTO.java
    ├── common/{Result, ResultCode, BusinessException, GlobalExceptionHandler}
    ├── interceptor/LoginInterceptor.java
    └── aspect/RequestLogAspect.java
src/main/resources
├── jdbc.properties / mybatis-config.xml / logback.xml
└── com/lab/mapper/EmpMapper.xml
sql/init.sql
```

## 五、运行方式

```bash
# 1. 单元测试（共 17 个，覆盖原生 MyBatis、动态 SQL、MP、SSM 整合与事务）
mvn test

# 2. 实验一入口程序（需先 mvn package 生成 target/emp-mis）
mvn package -DskipTests
java -cp "target/emp-mis/WEB-INF/classes;target/emp-mis/WEB-INF/lib/*" com.lab.runner.LabRunner

# 3. 实验二：将 target/emp-mis.war 部署到 Tomcat 9，或用 Smart Tomcat 插件运行
```

## 六、RESTful 接口清单

> 登录拦截器拦截除 `/auth/**` 外的所有请求，未登录返回 `{"code":401,...}`。
> 请先用 ApiFox 调 `POST /auth/login`（body：`{"username":"admin"}`）建立会话。

| 功能 | 方法 | 路径 |
|---|---|---|
| 登录 / 校验 / 退出 | POST / GET / POST | `/auth/login`、`/auth/check`、`/auth/logout` |
| 新增员工 | POST | `/emps` |
| 修改员工 | PUT | `/emps` |
| 删除员工 | DELETE | `/emps/{id}` |
| 批量删除 | DELETE | `/emps`（body：`[5,6]`） |
| 单条查询 | GET | `/emps/{id}` |
| 分页多条件查询 | GET | `/emps?page=1&size=5&name=&gender=&dept=&status=&minSalary=` |
| 薪资排行 | GET | `/emps/top?limit=3` |
| 事务回滚演示 | POST | `/emps/tx-demo` |

## 七、实验中踩到的真实问题（可直接写入实验报告）

1. **`PluginException: No @Intercepts annotation was found in interceptor MpPaginationPlugin`**
   原因：`@Intercepts` 注解不具备继承性，MyBatis 插件子类必须重新声明。
   解决：在子类上重写 `@Intercepts`（见 `MpPaginationPlugin` 注释）。

2. **分页插件"静默失效"：`total` 恒为 0、SQL 没有 `LIMIT`**
   原因：只声明了 `StatementHandler#prepare` 签名，而 MP 的分页逻辑由 `Executor#query` 触发。
   解决：完整复制 MP 父类上的 5 个 `@Signature`（prepare、getBoundSql、update、query×2）。

3. **`IllegalStateException: No ServletContext set`**
   原因：根容器 `@ComponentScan("com.lab")` 扫进了带 `@EnableWebMvc` 的 `WebMvcConfig`。
   解决：`excludeFilters` 中追加 `ASSIGNABLE_TYPE` 排除 `WebMvcConfig`。

4. **批量插入后拿不到自增主键**
   原因：`foreach` 批量 INSERT 不会触发 `useGeneratedKeys` 回填。
   解决：插入后按条件回查再删除（见 `EmpMapperXmlTest#testBatch`）。
