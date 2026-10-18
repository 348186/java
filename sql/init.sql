-- =============================================================
-- EMP 员工管理 MIS 系统 —— 数据库初始化脚本
-- 数据库：ssm_emp    编码：utf8mb4
-- 说明：表结构与初始数据与实验指导书完全一致，可直接重复执行
-- =============================================================

DROP DATABASE IF EXISTS ssm_emp;
CREATE DATABASE ssm_emp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssm_emp;

-- ---------------------------- 员工表 ----------------------------
DROP TABLE IF EXISTS emp;
CREATE TABLE emp (
    emp_id     INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工编号',
    emp_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender     CHAR(1)      DEFAULT '男' COMMENT '性别',
    dept       VARCHAR(50)  COMMENT '部门',
    post       VARCHAR(50)  COMMENT '岗位',
    salary     DECIMAL(10,2) COMMENT '薪资',
    hire_date  DATE         COMMENT '入职时间',
    status     TINYINT      DEFAULT 1 COMMENT '状态：1在职 0离职'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';

-- 任务 4.1：为 emp_name 建立普通索引，补充合理字段约束
ALTER TABLE emp
    MODIFY emp_name VARCHAR(50) NOT NULL COMMENT '姓名',
    MODIFY gender   CHAR(1)     NOT NULL DEFAULT '男' COMMENT '性别',
    MODIFY dept     VARCHAR(50) NOT NULL DEFAULT '待分配' COMMENT '部门',
    MODIFY post     VARCHAR(50) NOT NULL DEFAULT '待定岗' COMMENT '岗位',
    MODIFY salary   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '薪资',
    MODIFY status   TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1在职 0离职';

CREATE INDEX idx_emp_name ON emp (emp_name);
CREATE INDEX idx_emp_dept ON emp (dept);

-- ---------------------------- 初始数据 ----------------------------
-- 前 4 条与实验指导书「步骤 1.1」逐字一致；其后为自主扩充数据（部门/岗位/薪资风格保持一致）
INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('张伟', '男', '研发部', 'Java工程师', 12000.00, '2024-07-01', 1),
('李娜', '女', '研发部', '前端工程师', 10000.00, '2024-08-15', 1),
('王强', '男', '市场部', '市场专员', 8000.00, '2023-03-10', 1),
('赵敏', '女', '人事部', '人事专员', 7500.00, '2022-06-20', 0),
-- ---------------------- 以下为新增数据 ----------------------
('张三', '男', '研发部', '后端工程师', 13500.00, '2023-05-08', 1),
('李四', '女', '市场部', '市场经理', 15000.00, '2021-09-12', 1),
('刘岩', '男', '财务部', '会计', 9500.00, '2022-11-20', 1),
('权小乐', '女', '人事部', '招聘专员', 8200.00, '2024-03-04', 1),
('孙浩', '男', '研发部', '前端工程师', 11500.00, '2023-08-16', 1),
('周雨欣', '女', '测试部', '测试工程师', 9800.00, '2024-01-09', 1),
('吴俊杰', '男', '运维部', '运维工程师', 10500.00, '2022-04-25', 1),
('郑晓彤', '女', '研发部', '数据工程师', 12800.00, '2025-02-18', 1),
('冯磊', '男', '市场部', '销售代表', 7600.00, '2021-06-30', 0),
('何静怡', '女', '财务部', '出纳', 7200.00, '2023-10-11', 0);

-- 校验
SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status FROM emp ORDER BY emp_id;
