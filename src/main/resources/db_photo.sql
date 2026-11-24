-- ============================================================
-- 摄影工作室作品管理 MIS 数据库脚本
-- 数据库名：photo_studio
-- 业务载体：photo_work（摄影作品表）
-- 说明：替换原实验的 emp 员工表，字段语义全部改为摄影业务
-- ============================================================

DROP DATABASE IF EXISTS photo_studio;
CREATE DATABASE photo_studio DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE photo_studio;

CREATE TABLE photo_work (
    work_id       INT PRIMARY KEY AUTO_INCREMENT          COMMENT '作品编号',
    work_title    VARCHAR(80)  NOT NULL                   COMMENT '作品标题',
    photographer  VARCHAR(50)  NOT NULL                   COMMENT '摄影师',
    shoot_type    VARCHAR(40)  DEFAULT '人像'              COMMENT '拍摄题材：人像/风光/静物/街拍',
    studio        VARCHAR(60)  COMMENT '所属影棚/外景地',
    shoot_date    DATE         COMMENT '拍摄日期',
    retouch_hours DECIMAL(5,1) DEFAULT 0.0                COMMENT '后期修图工时（小时）',
    price         DECIMAL(10,2) COMMENT '作品定价（元）',
    status        TINYINT      DEFAULT 1                  COMMENT '状态：1可展示 0已下架'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄影作品表';

INSERT INTO photo_work
    (work_title, photographer, shoot_type, studio, shoot_date, retouch_hours, price, status)
VALUES
    ('晨雾中的白桦林', '林越',   '风光', 'Nordic外景基地',   '2025-03-18', 4.5, 1680.00, 1),
    ('午后窗边的少女', '苏晴',   '人像', 'A影棚',            '2025-04-02', 6.0, 2280.00, 1),
    ('旧巷光影',       '程野',   '街拍', '老城区外景',       '2025-04-21', 3.2, 980.00,  1),
    ('静物：玻璃与光', '林越',   '静物', 'B影棚',            '2025-05-06', 2.5, 760.00,  0),
    ('海边的风',       '苏晴',   '人像', '海滨外景基地',     '2025-05-19', 7.5, 2980.00, 1),
    ('城市夜色',       '程野',   '街拍', 'CBD外景',          '2025-06-11', 5.0, 1580.00, 1);

SELECT work_id, work_title, photographer, shoot_type, shoot_date, price, status
FROM photo_work ORDER BY work_id;
