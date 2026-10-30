-- ============================================================
-- 开发环境演示数据（密码均为 123456）
-- 生产环境请勿执行本脚本
-- ============================================================

USE `community_repair`;

-- admin      客服（CS）
-- worker1    维修工（WORKER），技能：电工
-- worker2    维修工（WORKER），技能：水工,木工
-- owner1     业主（OWNER），B3-1201
-- community1 社区工作人员（COMMUNITY）
INSERT INTO `users` (`username`, `password`, `real_name`, `role`, `phone`, `building`, `room`, `skill_tags`) VALUES
('admin',      '$2a$10$bEF5wHIET.3S1uuM925jNeAVrGCJZlCrSI6hXTxwL.TG.yVn/BC02', '张客服', 'CS',        '13800000001', NULL, NULL, NULL),
('community1', '$2a$10$bEF5wHIET.3S1uuM925jNeAVrGCJZlCrSI6hXTxwL.TG.yVn/BC02', '李社区', 'COMMUNITY', '13800000002', NULL, NULL, NULL),
('worker1',    '$2a$10$bEF5wHIET.3S1uuM925jNeAVrGCJZlCrSI6hXTxwL.TG.yVn/BC02', '王师傅', 'WORKER',    '13800000003', NULL, NULL, '电工'),
('worker2',    '$2a$10$bEF5wHIET.3S1uuM925jNeAVrGCJZlCrSI6hXTxwL.TG.yVn/BC02', '赵师傅', 'WORKER',    '13800000004', NULL, NULL, '水工,木工'),
('owner1',     '$2a$10$bEF5wHIET.3S1uuM925jNeAVrGCJZlCrSI6hXTxwL.TG.yVn/BC02', '陈业主', 'OWNER',     '13800000005', 'B3', '1201', NULL)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);
