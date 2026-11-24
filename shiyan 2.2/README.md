# 实验二：AI 赋能 MyBatis / MyBatis-Plus（摄影工作室版）

> 业务载体：`photo_studio` 库中的 `photographer`（摄影师表）与 `photo_work`（作品表，`photographer_id` 关联摄影师）。
> 本实现与原实验的 `emp` 员工表在表结构、字段语义、SQL、实体命名与测试数据上均不同。

## 一、环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0（当前配置账号 `root` / 密码 `348186`，如不同请修改 `mybatis-config.xml` 与 `MybatisPlusUtil.java`）
- IDE：IntelliJ IDEA 或 Eclipse

## 二、运行步骤

1. 初始化数据库：

   ```bash
   mysql -u root -p < src/main/resources/db_photo.sql
   ```

2. 编译与测试：

   ```bash
   mvn test
   ```

3. 运行手写 MyBatis 演示：

   ```bash
   mvn exec:java -Dexec.mainClass=com.lab.Main
   ```

4. 运行 MyBatis-Plus 演示：

   ```bash
   mvn exec:java -Dexec.mainClass=com.lab.PlusMain
   ```

> 若 `exec` 插件未配置，可在 IDE 中直接运行 `Main` / `PlusMain` 的 main 方法。

## 三、项目结构

```
src/main/java/com/lab/
├── Main.java                     # 手写 XML 版演示入口
├── PlusMain.java                 # MyBatis-Plus 版演示入口
├── entity/
│   ├── PhotoWork.java            # XML 版实体（POJO）
│   └── PhotoWorkPlus.java        # MP 版实体（注解映射）
├── mapper/
│   ├── PhotoWorkMapper.java      # 手写接口（12 个方法）
│   ├── PhotoWorkMapper.xml       # 动态 SQL 映射文件
│   └── PhotoWorkPlusMapper.java  # 继承 BaseMapper
└── util/
    ├── MyBatisUtil.java          # SqlSessionFactory 单例
    └── MybatisPlusUtil.java      # MP 工厂 + 分页插件
src/main/resources/
├── db_photo.sql                  # 建库建表与测试数据
├── mybatis-config.xml            # 手写 MyBatis 全局配置
└── log4j2.xml                    # Log4j2 SQL 日志
src/test/java/com/lab/
├── PhotoWorkMapperTest.java      # 8 个测试用例
└── PhotoWorkPlusTest.java        # 3 个测试用例
```

## 四、关键实现点

- `#{}` 预编译占位符（防 SQL 注入）；动态排序字段才使用 `${}` 并做白名单校验。
- `SqlSessionFactory` 全局唯一，`SqlSession` 非线程安全且用完关闭。
- 动态 SQL：`if/where`、`set`、`foreach`、`choose/when/otherwise`。
- MyBatis-Plus：`BaseMapper` 零 SQL CRUD、条件构造器 `QueryWrapper`、`PaginationInnerInterceptor`。
- 联表查询：`photo_work` 通过 `photographer_id` 关联 `photographer`，`selectAll/selectById` 使用 `LEFT JOIN` 带出摄影师姓名。
- 日志：Log4j2 输出 Mapper 包 DEBUG 级别 SQL 与参数，便于截图与排错。
