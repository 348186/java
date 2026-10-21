-- =============================================================
-- 摄影工作室管理系统 —— 数据库初始化脚本
-- 数据库：photo_studio    编码：utf8mb4
-- 说明：可直接重复执行（先删库再建库）
--       1) photographer 摄影师表：主表
--       2) photo_work   摄影作品表：从表，photographer_id 外键关联摄影师
-- =============================================================

DROP DATABASE IF EXISTS photo_studio;
CREATE DATABASE photo_studio DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE photo_studio;

-- ---------------------------- 摄影师表 ----------------------------
DROP TABLE IF EXISTS photographer;
CREATE TABLE photographer (
    photographer_id     INT PRIMARY KEY AUTO_INCREMENT COMMENT '摄影师编号',
    photographer_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender              CHAR(1)          DEFAULT '男' COMMENT '性别',
    studio              VARCHAR(50)  COMMENT '所属工作室',
    specialty           VARCHAR(50)  COMMENT '擅长领域',
    day_rate            DECIMAL(10,2) COMMENT '日薪',
    join_date           DATE         COMMENT '入驻日期',
    status              TINYINT      DEFAULT 1 COMMENT '状态：1在职 0离职'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '摄影师表';

-- 补充约束与索引（对应指导书任务 4.1）
ALTER TABLE photographer
    MODIFY photographer_name VARCHAR(50) NOT NULL COMMENT '姓名',
    MODIFY gender            CHAR(1)     NOT NULL DEFAULT '男' COMMENT '性别',
    MODIFY studio            VARCHAR(50) NOT NULL DEFAULT '待分配' COMMENT '所属工作室',
    MODIFY specialty         VARCHAR(50) NOT NULL DEFAULT '待定领域' COMMENT '擅长领域',
    MODIFY day_rate          DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '日薪',
    MODIFY status            TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1在职 0离职';

CREATE INDEX idx_photographer_name ON photographer (photographer_name);
CREATE INDEX idx_photographer_studio ON photographer (studio);

-- ---------------------------- 摄影作品表 ----------------------------
DROP TABLE IF EXISTS photo_work;
CREATE TABLE photo_work (
    work_id         INT PRIMARY KEY AUTO_INCREMENT COMMENT '作品编号',
    photographer_id INT          NOT NULL COMMENT '摄影师编号（外键）',
    work_title      VARCHAR(100) NOT NULL COMMENT '作品名称',
    location        VARCHAR(100) COMMENT '拍摄地',
    shoot_date      DATE         COMMENT '拍摄日期',
    style           VARCHAR(50)  COMMENT '风格：人像/风光/婚礼/纪实/商业/旅拍/航拍/星空/建筑/儿童/美食/宠物/后期',
    price           DECIMAL(10,2) COMMENT '报价',
    status          TINYINT      DEFAULT 1 COMMENT '状态：1已上架 0已下架',
    CONSTRAINT fk_work_photographer
        FOREIGN KEY (photographer_id) REFERENCES photographer (photographer_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '摄影作品表';

ALTER TABLE photo_work
    MODIFY work_title VARCHAR(100) NOT NULL COMMENT '作品名称',
    MODIFY style      VARCHAR(50)  NOT NULL DEFAULT '未分类' COMMENT '风格',
    MODIFY price      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '报价',
    MODIFY status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1已上架 0已下架';

CREATE INDEX idx_work_title ON photo_work (work_title);
CREATE INDEX idx_work_photographer ON photo_work (photographer_id);
CREATE INDEX idx_work_style ON photo_work (style);

-- ---------------------------- 摄影师初始数据（14 位） ----------------------------
INSERT INTO photographer (photographer_name, gender, studio, specialty, day_rate, join_date, status) VALUES
('张伟', '男', '人像工作室', '人像摄影', 12000.00, '2024-07-01', 1),
('李娜', '女', '人像工作室', '风光摄影', 10000.00, '2024-08-15', 1),
('王强', '男', '风光影像组', '婚礼跟拍', 8000.00,  '2023-03-10', 1),
('赵敏', '女', '商业广告组', '商业摄影', 7500.00,  '2022-06-20', 0),
('张三', '男', '人像工作室', '无人机航拍', 13500.00, '2023-05-08', 1),
('李四', '女', '风光影像组', '旅拍摄影', 15000.00, '2021-09-12', 1),
('刘岩', '男', '婚礼纪实组', '后期修图', 9500.00,  '2022-11-20', 1),
('权小乐', '女', '商业广告组', '纪实摄影', 8200.00, '2024-03-04', 1),
('孙浩', '男', '人像工作室', '风光摄影', 11500.00, '2023-08-16', 1),
('周雨欣', '女', '旅拍小队', '儿童摄影', 9800.00,  '2024-01-09', 1),
('吴俊杰', '男', '旅拍小队', '星空摄影', 10500.00, '2022-04-25', 1),
('郑晓彤', '女', '人像工作室', '建筑摄影', 12800.00, '2025-02-18', 1),
('冯磊', '男', '风光影像组', '美食摄影', 7600.00,  '2021-06-30', 0),
('何静怡', '女', '婚礼纪实组', '宠物摄影', 7200.00,  '2023-10-11', 0);

-- ---------------------------- 作品初始数据（23 件） ----------------------------
INSERT INTO photo_work (photographer_id, work_title, location, shoot_date, style, price, status) VALUES
(1,  '晨光里的她',   '上海 武康路',       '2024-07-20', '人像', 1200.00, 1),
(1,  '城市夜行者',   '上海 外滩',         '2024-09-05', '人像', 1500.00, 1),
(1,  '胶片肖像集',   '上海 田子坊',       '2025-01-18', '人像',  980.00, 1),
(2,  '雪山黎明',     '四川 贡嘎',         '2024-08-20', '风光', 2600.00, 1),
(2,  '海边银河',     '福建 平潭',         '2024-10-02', '风光', 2200.00, 1),
(3,  '海岛婚礼',     '三亚 蜈支洲岛',     '2023-05-01', '婚礼', 3800.00, 1),
(3,  '教堂誓言',     '北京 王府井教堂',   '2023-09-16', '婚礼', 3200.00, 1),
(4,  '冬季新品大片', '北京 798',          '2022-08-08', '商业', 4500.00, 0),
(5,  '云端之城',     '深圳 福田',         '2023-06-11', '航拍', 3000.00, 1),
(5,  '梯田之镜',     '云南 元阳',         '2023-11-25', '航拍', 2800.00, 1),
(6,  '京都秋日',     '日本 京都',         '2021-10-15', '旅拍', 5200.00, 1),
(6,  '冰岛极光',     '冰岛 雷克雅未克',   '2022-02-08', '旅拍', 6800.00, 1),
(7,  '复古调色样片', '广州 沙面',         '2022-12-01', '后期',  900.00, 1),
(8,  '老街理发店',   '成都 宽窄巷子',     '2024-04-12', '纪实', 1100.00, 1),
(8,  '早市烟火',     '成都 玉林',         '2024-06-30', '纪实', 1000.00, 1),
(9,  '雾锁群山',     '湖南 张家界',       '2023-09-02', '风光', 2400.00, 1),
(10, '第一次奔跑',   '杭州 西湖',         '2024-02-20', '儿童',  880.00, 1),
(10, '生日气球',     '杭州 滨江',         '2024-05-06', '儿童',  760.00, 1),
(11, '银河拱桥',     '青海 茶卡',         '2022-07-15', '星空', 3600.00, 1),
(12, '几何立面',     '上海 陆家嘴',       '2025-03-08', '建筑', 2600.00, 1),
(12, '老厂房改造',   '上海 杨浦',         '2025-04-21', '建筑', 2100.00, 1),
(13, '深夜食堂',     '武汉 吉庆街',       '2021-08-19', '美食',  700.00, 0),
(14, '猫的午后',     '武汉 光谷',         '2022-05-11', '宠物',  600.00, 0);

-- ---------------------------- 校验 ----------------------------
SELECT photographer_id, photographer_name, gender, studio, specialty, day_rate, join_date, status
FROM photographer ORDER BY photographer_id;

SELECT work_id, photographer_id, work_title, location, shoot_date, style, price, status
FROM photo_work ORDER BY work_id;

-- 多表联查示例（与 PhotoWorkMapper 中的查询等价）
SELECT w.work_id, w.work_title, w.style, w.price, p.photographer_name, p.studio
FROM photo_work w
         INNER JOIN photographer p ON w.photographer_id = p.photographer_id
ORDER BY w.price DESC;
