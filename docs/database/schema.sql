-- ============================================================
-- 物业工单通 · 基础表结构（阶段 P0）
-- MySQL 8.0+，字符集统一 utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `community_repair`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `community_repair`;

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
-- 流程状态 status 与费用状态 fee_status 解耦：
--   流程：PENDING → ASSIGNED → REPAIRING → VERIFYING → ARCHIVED
--   费用：NONE → PENDING → PAID
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tickets` (
    `id`                 BIGINT      NOT NULL AUTO_INCREMENT,
    `ticket_no`          VARCHAR(30) NOT NULL COMMENT '对外单号',
    `owner_id`           BIGINT      NOT NULL COMMENT '报修业主',
    `building`           VARCHAR(50) DEFAULT NULL,
    `room`               VARCHAR(20) DEFAULT NULL,
    `category`           VARCHAR(50) DEFAULT NULL COMMENT '故障分类，可由模型辅助生成',
    `description`        TEXT,
    `media_urls`         JSON        DEFAULT NULL COMMENT '图片/视频访问地址数组',
    `status`             ENUM('PENDING','ASSIGNED','REPAIRING','VERIFYING','ARCHIVED')
                                     NOT NULL DEFAULT 'PENDING',
    `priority`           ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
    `assigned_worker_id` BIGINT      DEFAULT NULL,
    `fee_status`         ENUM('NONE','PENDING','PAID') NOT NULL DEFAULT 'NONE',
    `created_at`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tickets_no` (`ticket_no`),
    KEY `idx_tickets_owner` (`owner_id`, `created_at`),
    KEY `idx_tickets_status` (`status`, `created_at`),
    KEY `idx_tickets_worker` (`assigned_worker_id`, `status`),
    KEY `idx_tickets_fee` (`fee_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单';

-- ------------------------------------------------------------
-- 工单流转日志：进度透明与时间线的数据基础
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ticket_logs` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `ticket_id`   BIGINT       NOT NULL,
    `from_status` VARCHAR(20)  DEFAULT NULL COMMENT '首次创建时为空',
    `to_status`   VARCHAR(20)  NOT NULL,
    `operator_id` BIGINT       DEFAULT NULL,
    `remark`      VARCHAR(500) DEFAULT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_logs_ticket` (`ticket_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志';
