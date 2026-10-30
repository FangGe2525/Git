-- ============================================================
-- 物业工单通 · 表结构 v2（阶段 P2）
-- MySQL 8.0+，字符集统一 utf8mb4
--
-- 注意：tickets / ticket_logs / reviews 为开发期重建，
--       执行本脚本会清空这三张表的既有数据。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `community_repair`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `community_repair`;

DROP TABLE IF EXISTS `ticket_logs`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `tickets`;

-- ------------------------------------------------------------
-- 用户表
-- role: OWNER 业主 / CS 客服 / WORKER 维修工 / COMMUNITY 社区工作人员
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `real_name`  VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `role`       ENUM('OWNER','CS','WORKER','COMMUNITY') NOT NULL,
    `phone`      VARCHAR(20)  DEFAULT NULL,
    `building`   VARCHAR(50)  DEFAULT NULL COMMENT '楼栋',
    `room`       VARCHAR(20)  DEFAULT NULL COMMENT '房号',
    `skill_tags` VARCHAR(200) DEFAULT NULL COMMENT '维修工技能标签，逗号分隔',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    KEY `idx_users_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ------------------------------------------------------------
-- 工单表
-- 流程状态 status：PENDING → ACCEPTED → REPAIRING → VERIFYING → COMPLETED
--                  任意非终态可 → CANCELED
-- 费用状态 fee_status 与流程状态解耦：NONE → PENDING → PAID
-- 评价不设状态位，由 reviews 表是否存在记录体现
-- ------------------------------------------------------------
CREATE TABLE `tickets` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `ticket_no`          VARCHAR(30)  NOT NULL COMMENT '对外单号 WX+日期+序号',
    `source`             ENUM('SCAN','PHONE','WALK_IN','OTHER') NOT NULL DEFAULT 'SCAN'
                                      COMMENT '报修来源：扫码/电话代报/上门/其他',
    `owner_id`           BIGINT       DEFAULT NULL COMMENT '报修业主；客服代报时可为空',
    `reporter_name`      VARCHAR(50)  DEFAULT NULL COMMENT '报修人姓名（代报时必填）',
    `reporter_phone`     VARCHAR(20)  DEFAULT NULL COMMENT '报修人电话',
    `building`           VARCHAR(50)  DEFAULT NULL COMMENT '楼栋',
    `unit`               VARCHAR(20)  DEFAULT NULL COMMENT '单元',
    `room`               VARCHAR(20)  DEFAULT NULL COMMENT '房号',
    `location_code`      VARCHAR(50)  DEFAULT NULL COMMENT '二维码点位编码',
    `location_detail`    VARCHAR(200) DEFAULT NULL COMMENT '具体位置描述',
    `category`           VARCHAR(50)  DEFAULT NULL COMMENT '工种：水电/瓦工/电梯…',
    `problem_type`       VARCHAR(50)  DEFAULT NULL COMMENT '问题分类：漏水/断电…',
    `description`        TEXT,
    `media_urls`         JSON         DEFAULT NULL COMMENT '报修时上传的图片/视频地址数组',
    `finish_media_urls`  JSON         DEFAULT NULL COMMENT '维修工完工提交的照片，业主确认时可见',
    `status`             ENUM('PENDING','ACCEPTED','REPAIRING','VERIFYING','COMPLETED','CANCELED')
                                      NOT NULL DEFAULT 'PENDING',
    `priority`           ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
    `assigned_worker_id` BIGINT       DEFAULT NULL COMMENT '当前负责维修工',
    `accepted_at`        DATETIME     DEFAULT NULL COMMENT '接单时间',
    `arrived_at`         DATETIME     DEFAULT NULL COMMENT '到场/开始处理时间',
    `finished_at`        DATETIME     DEFAULT NULL COMMENT '完工提交时间',
    `confirmed_at`       DATETIME     DEFAULT NULL COMMENT '业主确认时间',
    `fee_status`         ENUM('NONE','PENDING','PAID') NOT NULL DEFAULT 'NONE',
    `fee_amount`         DECIMAL(10,2) DEFAULT NULL COMMENT '有偿维修金额',
    `rework_count`       INT          NOT NULL DEFAULT 0 COMMENT '返修次数',
    `parent_ticket_id`   BIGINT       DEFAULT NULL COMMENT '返修来源单（预留）',
    `cancel_reason`      VARCHAR(200) DEFAULT NULL,
    `cancel_by`          BIGINT       DEFAULT NULL,
    `created_by`         BIGINT       DEFAULT NULL COMMENT '代报修客服 id',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tickets_no` (`ticket_no`),
    KEY `idx_tickets_owner` (`owner_id`, `created_at`),
    KEY `idx_tickets_status` (`status`, `created_at`),
    KEY `idx_tickets_worker` (`assigned_worker_id`, `status`),
    KEY `idx_tickets_fee` (`fee_status`),
    KEY `idx_tickets_location` (`location_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单';

-- ------------------------------------------------------------
-- 工单日志：状态变更与关键动作（派单/催办/备注）都在这里留痕
-- action 取值见 TicketAction 枚举
-- ------------------------------------------------------------
CREATE TABLE `ticket_logs` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `ticket_id`   BIGINT       NOT NULL,
    `action`      VARCHAR(30)  NOT NULL COMMENT 'CREATE/ASSIGN/REASSIGN/ACCEPT/START/FINISH/CONFIRM/REWORK/CANCEL/REMARK/URGE',
    `from_status` VARCHAR(20)  DEFAULT NULL COMMENT '首次创建时为空',
    `to_status`   VARCHAR(20)  DEFAULT NULL,
    `operator_id` BIGINT       DEFAULT NULL,
    `remark`      VARCHAR(500) DEFAULT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_logs_ticket` (`ticket_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志';

-- ------------------------------------------------------------
-- 评价（P5 使用）：三维评分，单独成表，不占用工单状态
-- ------------------------------------------------------------
CREATE TABLE `reviews` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `ticket_id`      BIGINT       NOT NULL,
    `owner_id`       BIGINT       NOT NULL,
    `speed_score`    TINYINT      NOT NULL COMMENT '响应速度 1-5',
    `quality_score`  TINYINT      NOT NULL COMMENT '维修质量 1-5',
    `attitude_score` TINYINT      NOT NULL COMMENT '服务态度 1-5',
    `comment`        VARCHAR(500) DEFAULT NULL,
    `follow_up`      VARCHAR(500) DEFAULT NULL COMMENT '客服回访记录',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reviews_ticket` (`ticket_id`),
    KEY `idx_reviews_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单评价';
