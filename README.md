# 摄影工作室管理实验工程（MyBatis + MyBatis-Plus）

对应《JAVA框架技术（一）》（AI 赋能版）课程实验指导书：

- **实验一**：AI 赋能 MyBatis 与 MyBatis-Plus 环境搭建及 CRUD 实操
- **实验三**：MyBatis 缓存机制（一级缓存 / 二级缓存 / 自研 TTL-LRU 缓存）
- **多表查询**：摄影作品 `photo_work` × 摄影师 `photographer` 的联表、嵌套与聚合查询

> 工程当前只保留实验一（原生 MyBatis + MyBatis-Plus CRUD）与实验三（缓存机制），
> **实验二（SSM 框架整合 / Web 接口）相关代码已按要求移除**。

> 业务主题已整体改造为「摄影工作室」：主表 `photographer`（摄影师）、从表 `photo_work`（摄影作品），
> 两张表通过 `photographer_id` 外键关联，构成一对多关系，用于演示多表查询。

---

## 一、环境要求

| 类别 | 版本 |
|---|---|
| JDK | 11（当前机器为 JDK 25，编译参数 `--release 11`） |
| Maven | 3.8.x + 阿里云镜像 |
| MySQL | 8.0 / 9.x 均可 |
| 依赖 | MyBatis 3.5.13、MyBatis-Plus 3.5.5、mysql-connector-j 8.0.33、Log4j 1.2.17、JUnit 4.13.2 |

## 二、数据库初始化

```bash
mysql -uroot -p < sql/init.sql
```

库名 `photo_studio`，共两张表：

| 表名 | 说明 | 行数 |
|---|---|---|
| `photographer` | 摄影师（主表） | 14 |
| `photo_work` | 摄影作品（从表，`photographer_id` 外键） | 23 |

**`photographer` 摄影师表**（工作室、擅长领域、日薪风格保持一致）：

| photographer_id | photographer_name | gender | studio | specialty | day_rate | join_date | status | 来源 |
|---|---|---|---|---|---|---|---|---|
| 1 | 张伟 | 男 | 人像工作室 | 人像摄影 | 12000.00 | 2024-07-01 | 1 | 指导书 |
| 2 | 李娜 | 女 | 人像工作室 | 风光摄影 | 10000.00 | 2024-08-15 | 1 | 指导书 |
| 3 | 王强 | 男 | 风光影像组 | 婚礼跟拍 | 8000.00 | 2023-03-10 | 1 | 指导书 |
| 4 | 赵敏 | 女 | 商业广告组 | 商业摄影 | 7500.00 | 2022-06-20 | 0 | 指导书 |
| 5 | 张三 | 男 | 人像工作室 | 无人机航拍 | 13500.00 | 2023-05-08 | 1 | 扩充 |
| 6 | 李四 | 女 | 风光影像组 | 旅拍摄影 | 15000.00 | 2021-09-12 | 1 | 扩充 |
| 7 | 刘岩 | 男 | 婚礼纪实组 | 后期修图 | 9500.00 | 2022-11-20 | 1 | 扩充 |
| 8 | 权小乐 | 女 | 商业广告组 | 纪实摄影 | 8200.00 | 2024-03-04 | 1 | 扩充 |
| 9 | 孙浩 | 男 | 人像工作室 | 风光摄影 | 11500.00 | 2023-08-16 | 1 | 扩充 |
| 10 | 周雨欣 | 女 | 旅拍小队 | 儿童摄影 | 9800.00 | 2024-01-09 | 1 | 扩充 |
| 11 | 吴俊杰 | 男 | 旅拍小队 | 星空摄影 | 10500.00 | 2022-04-25 | 1 | 扩充 |
| 12 | 郑晓彤 | 女 | 人像工作室 | 建筑摄影 | 12800.00 | 2025-02-18 | 1 | 扩充 |
| 13 | 冯磊 | 男 | 风光影像组 | 美食摄影 | 7600.00 | 2021-06-30 | 0 | 扩充 |
| 14 | 何静怡 | 女 | 婚礼纪实组 | 宠物摄影 | 7200.00 | 2023-10-11 | 0 | 扩充 |

**`photo_work` 摄影作品表**（节选，完整数据见 `sql/init.sql`）：

| work_id | photographer_id | work_title | location | shoot_date | style | price | status |
|---|---|---|---|---|---|---|---|
| 1 | 1 | 晨光里的她 | 上海 武康路 | 2024-07-20 | 人像 | 1200.00 | 1 |
| 4 | 2 | 雪山黎明 | 四川 贡嘎 | 2024-08-20 | 风光 | 2600.00 | 1 |
| 6 | 3 | 海岛婚礼 | 三亚 蜈支洲岛 | 2023-05-01 | 婚礼 | 3800.00 | 1 |
| 8 | 4 | 冬季新品大片 | 北京 798 | 2022-08-08 | 商业 | 4500.00 | 0 |
| 11 | 6 | 京都秋日 | 日本 京都 | 2021-10-15 | 旅拍 | 5200.00 | 1 |
| 19 | 11 | 银河拱桥 | 青海 茶卡 | 2022-07-15 | 星空 | 3600.00 | 1 |
| 22 | 13 | 深夜食堂 | 武汉 吉庆街 | 2021-08-19 | 美食 | 700.00 | 0 |

> 单元测试不写死行数，而是与 MP 的 `selectCount()` 交叉校验，
> 因此后续继续增删摄影师 / 作品数据时测试仍然有效。

连接四要素在 `src/main/resources/jdbc.properties` 中修改（**禁止硬编码到 Java 代码**）。

## 三、与指导书的差异化对照

### 3.1 依赖版本（实验一严格对齐指导书）

指导书实验一（步骤 1.5 + 步骤 4.1）给出的依赖只有 5 个，本工程已全部对齐：

| 依赖 | 指导书版本 | 本工程版本 | 说明 |
|---|---|---|---|
| mybatis | 3.5.13 | **3.5.13** | 与指导书一致 |
| mybatis-plus | 3.5.3.1 | **3.5.5**（唯一差异） | 见下方说明 |
| mysql-connector-j | 8.0.33 | **8.0.33** | 与指导书一致 |
| log4j | 1.2.17 | **1.2.17** | 与指导书一致，配置见 `log4j.properties` |
| junit | 4.13.2 | **4.13.2** | 与指导书一致，测试全部用 JUnit 4 编写 |

> **为什么 mybatis-plus 升到 3.5.5**：3.5.3.1 在没有 Spring 的纯 MyBatis 工程里，
> 初始化 `BaseMapper` 时会走 `SpringReflectionHelper` 而抛
> `NoClassDefFoundError: org/springframework/core/GenericTypeResolver`；
> 3.5.5 修掉了这个问题，且与 mybatis 3.5.13 兼容（35 个测试全部通过）。
> 若坚持 3.5.3.1，就必须额外引入 `spring-core`，反而违背「只用指导书依赖」。

**额外保留（仅实验三缓存机制使用，指导书实验一未涉及）**：`slf4j 1.7.36` + `logback 1.2.13`。

**已从工程中移除**：`druid`（改用 MyBatis 内置 `POOLED` 数据源）、
`hibernate-validator`（去掉实体上的 JSR-303 注解）、`junit-jupiter`（改为 JUnit 4）。

### 3.2 代码层面的区分

| 指导书示例 | 本工程实现 |
|---|---|
| `MyBatisUtil` | `MybatisSessionUtil`（静态块构建工厂，含 `openSession(autoCommit)` 重载） |
| `PhotographerMapper.selectById/insert/update/deleteById` | `findByPrimaryKey / addPhotographer / modifyPhotographer / removePhotographer`（避免与 `BaseMapper` 重载歧义） |
| `selectByCondition / updateDynamic / deleteBatch / insertBatch` | `queryByExample / modifySelective / removeInBatch / addInBatch` |
| 分页插件在 XML 中写 `<property name="@...PaginationInnerInterceptor"/>`（MP 3.4 旧写法） | 自定义 `MpPaginationPlugin` 子类，构造器内装配 `PaginationInnerInterceptor(DbType.MYSQL)` |
| `Photographer.hireDate` 用 `java.util.Date` | 用 `java.time.LocalDate`（JDK8 时间 API） |
| `Photographer.status` 标注 `@TableLogic` | 不启用逻辑删除（避免把「离职」误当成「已删除」而查不到赵敏） |
| `PhotographerMapper.xml` 放 `resources/mapper/` | 放 `resources/com/lab/mapper/`，支持 `<package>` 包扫描 |

### 3.3 数据库保持一致

`sql/init.sql` 完整包含库、两张表、索引、外键约束与初始数据，可重复执行（先删库再建库）：

- `photographer` 14 位摄影师，前 4 条沿用指导书原有人员，其余 10 条为扩充（工作室 / 擅长领域风格一致）；
- `photo_work` 23 件作品，通过 `photographer_id` 外键挂到摄影师名下，用于多表查询；
- 索引：`idx_photographer_name`、`idx_photographer_studio`、`idx_work_title`、`idx_work_style`，
  并补充了非空 / 默认值约束（对应指导书任务 4.1）。

## 四、目录结构

```
src/main/java/com/lab
├── entity/Photographer.java         # 摄影师实体（MP 注解 + 一对多 works）
├── entity/PhotoWork.java            # 摄影作品实体（外键 photographer_id）
├── vo/WorkDetailVO.java             # 多表联查 VO：作品 + 摄影师
├── vo/StudioStatVO.java             # 分组聚合 VO：工作室维度统计
├── dto/WorkQueryDTO.java            # 多表动态条件查询入参
├── mapper/PhotographerMapper.java   # BaseMapper + 动态 SQL + findWithWorks（一对多）
├── mapper/PhotoWorkMapper.java      # 多表查询：联表 / 动态条件 / 分组聚合
├── util/MybatisSessionUtil.java     # 实验一会话工具类
├── plugin/MpPaginationPlugin.java   # 分页插件宿主
├── runner/LabRunner.java            # 实验一 main 入口（含多表查询演示）
├── cache/                           # 实验三 MyBatis 缓存机制
│   ├── TimedLruCache.java           # 自研二级缓存：带 TTL 的 LRU + 序列化拷贝
│   ├── PhotographerCacheMapper.java # 缓存实验专用 Mapper（XML 方式，独立 namespace）
│   ├── annotation/PhotographerCacheAnnotationMapper.java  # @CacheNamespace 注解方式
│   ├── CacheSessionFactory.java     # 每次新建一份干净的二级缓存
│   ├── SqlAccessCounter.java        # 统计实际发出的 SQL 条数（Logback Appender）
│   ├── CacheXmlDemo.java            # XML 方式演示入口
│   └── CacheAnnotationDemo.java     # 注解方式演示入口
src/main/resources
├── jdbc.properties / mybatis-config.xml       # 实验一：logImpl=LOG4J
├── log4j.properties                           # 实验一日志（指导书步骤 2.5）
├── logback.xml                                # 仅实验三缓存演示使用
├── com/lab/mapper/PhotographerMapper.xml
├── com/lab/mapper/PhotoWorkMapper.xml        # 多表查询 SQL（JOIN / collection / GROUP BY）
├── com/lab/cache/PhotographerCacheMapper.xml
└── cache/{xml, l1-only, statement, annotation}/mybatis-config.xml  # 实验三四套配置
src/test/java/com/lab
├── PhotographerMapperXmlTest.java   (8)      # 动态 SQL / CRUD / 批量 / 事务
├── PhotographerMybatisPlusTest.java (6)      # 通用 CRUD / Wrapper / 分页
├── MultiTableQueryTest.java         (5)      # 多表：联表 / 动态条件 / 聚合 / 一对多
├── PhotographerCacheTest.java      (10)      # 一二级缓存 / useCache / flushCache / CacheKey
└── TimedLruCacheTest.java           (6)      # LRU 淘汰 / TTL 过期 / 序列化副本 / 命中率
sql/init.sql
```

## 五、多表查询功能

`photo_work`（作品，多） × `photographer`（摄影师，一），外键 `photo_work.photographer_id`。

| 能力 | 方法 | SQL 要点 |
|---|---|---|
| 普通联表 | `PhotoWorkMapper.findWorkDetails()` | `INNER JOIN ... ON w.photographer_id = p.photographer_id` → `WorkDetailVO` |
| 动态条件联表 | `findWorkDetailsByCondition(WorkQueryDTO)` | `<where> + <if>`：风格、拍摄地模糊、摄影师姓名模糊、工作室、报价区间、上下架 |
| 按主表维度反查 | `findWorksByStudio(studio)` | 用摄影师表的字段过滤作品结果 |
| 分组聚合 | `statByStudio()` | `GROUP BY studio` + `COUNT/AVG/MAX/MIN` → `StudioStatVO` |
| 一对多嵌套 | `PhotographerMapper.findWithWorks()` | `LEFT JOIN` + `<collection>` 填充 `Photographer.works` |

> 两张表都有 `status` / `photographer_id` 列，联表必须起别名
> （`w.status AS work_status`、`w.photographer_id AS work_photographer_id`），否则从表列会覆盖主表列；
> `findWithWorks` 用 `LEFT JOIN`，保证暂未上传作品的摄影师也会出现在结果中。

## 六、运行方式

```bash
# 1. 单元测试（JUnit 4.13.2，共 35 个，覆盖原生 MyBatis、动态 SQL、MP、多表查询与缓存机制）
mvn test

# 2. 生成依赖 classpath（运行 main 入口前执行一次即可）
mvn package -DskipTests
mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt

# 3. 实验一入口程序（Windows 用 ; 分隔，Linux/Mac 用 :）
java -cp "target/classes;$(cat target/cp.txt)" com.lab.runner.LabRunner

# 4. 实验三缓存演示入口（两个）
java -cp "target/classes;$(cat target/cp.txt)" com.lab.cache.CacheXmlDemo
java -cp "target/classes;$(cat target/cp.txt)" com.lab.cache.CacheAnnotationDemo
```

## 七、实验中踩到的真实问题（可直接写入实验报告）

1. **`PluginException: No @Intercepts annotation was found in interceptor MpPaginationPlugin`**
   原因：`@Intercepts` 注解不具备继承性，MyBatis 插件子类必须重新声明。
   解决：在子类上重写 `@Intercepts`（见 `MpPaginationPlugin` 注释）。

2. **分页插件"静默失效"：`total` 恒为 0、SQL 没有 `LIMIT`**
   原因：只声明了 `StatementHandler#prepare` 签名，而 MP 的分页逻辑由 `Executor#query` 触发。
   解决：完整复制 MP 父类上的 5 个 `@Signature`（prepare、getBoundSql、update、query×2）。

3. **批量插入后拿不到自增主键**
   原因：`foreach` 批量 INSERT 不会触发 `useGeneratedKeys` 回填。
   解决：插入后按条件回查再删除（见 `PhotographerMapperXmlTest#testBatch`）。

4. **自定义 Cache 不生效 / 二级缓存「不返回副本」**
   原因：`CacheBuilder#build()` 只会对 `PerpetualCache` 追加 LRU / Serialized / Synchronized
   等装饰器，自定义实现只被包一层 `LoggingCache`（日志里可见 class 为
   `org.apache.ibatis.cache.decorators.LoggingCache`）。
   解决：自己在 `TimedLruCache` 里用 `ReentrantReadWriteLock` 保证线程安全，
   并在 `putObject` 时序列化、`getObject` 时反序列化，从而保持「返回副本」的语义。

5. **「commit 后一级缓存失效 → 重新发 SQL」这个结论不一定成立**
   原因：`commit()` 时会把本 Session 查过的数据一并写进二级缓存，
   一级缓存虽被清空，二级缓存却可能直接命中，于是看不到 SQL。
   解决：做纯一级缓存实验时统一使用 `cacheEnabled=false` 的配置
   （`cache/l1-only/mybatis-config.xml`）把变量隔离掉。

---

## 八、实验三：MyBatis 缓存机制

> 内容来源为教学课件《mybatis06 MyBatis 缓存机制》，本模块在理解的基础上重写实现，
> 代码结构、类名、示例表、验证方式与原文均不相同，详见下方对照。

### 8.1 核心结论速查

| 结论 | 验证位置 |
|---|---|
| 一级缓存默认开启、作用域为 `SqlSession`，保存的是对象引用（`s1 == s2` 为 `true`） | `PhotographerCacheTest#levelOneCacheReuseInSameSession` |
| 一级缓存跨 `SqlSession` 不共享 | `PhotographerCacheTest#levelOneCacheNotSharedBetweenSessions` |
| 增删改 / `clearCache()` / `commit()` / `rollback()` 都会清空一级缓存 | `PhotographerCacheTest#levelOneCacheInvalidation` |
| `localCacheScope=STATEMENT` 可禁用一级缓存（同一 Session 内也不复用） | `PhotographerCacheTest#levelOneCacheUnderStatementScope` |
| 二级缓存需 `<cache>` 或 `@CacheNamespace` 显式开启，且实体要实现 `Serializable` | `com/lab/cache/PhotographerCacheMapper.xml` |
| 未 `close()` / `commit()` 的 Session，其数据不会进入二级缓存 | `PhotographerCacheTest#levelTwoCacheRequiresCommitOrClose` |
| 二级缓存返回的是反序列化副本（`==` 为 `false`） | `PhotographerCacheTest#levelTwoCacheSharedAndReturnsCopy` |
| 写操作提交后清空本 namespace 的二级缓存 | `PhotographerCacheTest#levelTwoCacheClearedAfterWrite` |
| `useCache=false` 不参与缓存；`flushCache=true` 执行前清空缓存 | `PhotographerCacheTest#useCacheFalseIsNeverCached`、`#flushCacheTrueClearsCache` |
| CacheKey = statementId + SQL + 参数 + RowBounds，任一不同即为新条目 | `PhotographerCacheTest#cacheKeyDiffersByStatementAndParam` |

### 8.2 与源文件《mybatis06 MyBatis缓存机制.html》的差异

| 源文件 | 本项目实现 |
|---|---|
| 示例表 `student`，实体 `Student` | 复用本工程 `photo_studio.photographer` 表与 `Photographer` 实体（新增 `implements Serializable`） |
| Mapper 名 `StudentMapper`，方法 `selectById/selectAll` | `PhotographerCacheMapper`，方法 `findByPrimaryKey/findByDept/findAll/findAllFresh/renewSalary` |
| 二级缓存用内置 `<cache eviction="LRU">` | 自研 `TimedLruCache`：LRU + **TTL 过期** + 命中率统计 + 自行序列化保证副本语义 |
| 配置 2 套（xml / annotation） | 配置 **4 套**：`xml`(SESSION+L2)、`l1-only`(关闭 L2，隔离一级缓存实验)、`statement`(对照)、`annotation` |
| 靠肉眼看控制台日志判断是否走缓存 | 自写 `SqlAccessCounter`（挂在 Logback Appender 上）统计 SQL 条数，自动比对期望值并打 ✔/✘ |
| 只有 `main` 方法演示 | `main` 演示 **+ JUnit 4 自动化测试**（`PhotographerCacheTest` 10 个 + `TimedLruCacheTest` 6 个） |
| 日志用 LOG4J | 实验一用 LOG4J（`log4j.properties`）；实验三演示用 SLF4J + Logback（`logback.xml`） |
| 演示 `s1 == s2` 一级缓存引用相同 | 同时对比演示「一级缓存同一引用 vs 二级缓存副本」的差异 |
| 只讲解 `localCacheScope=STATEMENT` | 新增 STATEMENT 作用域**对照实验**（同一 Session 三次查询发 3 次 SQL） |
| 未讨论 Cache 装饰器细节 | 明确指出自定义 Cache 不会被自动加 `SynchronizedCache` / `SerializedCache`，并自行实现对应能力 |
| MyBatis 3.5.15（随 MyBatis-Plus 传递） | `pom.xml` 显式锁定 **MyBatis 3.5.16** |

### 8.3 运行缓存演示

```bash
mvn package -DskipTests
mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt
java -cp "target/classes;$(cat target/cp.txt)" com.lab.cache.CacheXmlDemo
java -cp "target/classes;$(cat target/cp.txt)" com.lab.cache.CacheAnnotationDemo
```

输出示例（已过滤 SQL 明细，只保留结论行）：

```
一、一级缓存（SqlSession 级，默认开启）
  · 同一 SqlSession 连续两次 findByPrimaryKey(1)        实际 SQL 1 次 / 期望 1 次  ✔
    两次结果是否为同一对象引用: true    → 一级缓存缓存的是对象本身
  · 两个 SqlSession 各查一次相同数据                     实际 SQL 2 次 / 期望 2 次  ✔
  · 执行 update（rollback）后再查                       实际 SQL 1 次 / 期望 1 次  ✔
  · session.clearCache() 后再查                       实际 SQL 1 次 / 期望 1 次  ✔

三、二级缓存（namespace 级，需显式开启）
    二级缓存实现: org.apache.ibatis.cache.decorators.LoggingCache
  · Session1.close() 后，Session2 查询相同数据          实际 SQL 0 次 / 期望 0 次  ✔
    两次结果是否为同一对象引用: false    → 二级缓存返回的是反序列化出来的副本
    Cache Hit Ratio [com.lab.cache.PhotographerCacheMapper]: 0.5
```

> 演示过程只对 photographer 表执行「写回原值再提交/回滚」的操作，运行前后数据完全一致。
