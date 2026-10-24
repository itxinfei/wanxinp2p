-- 实名活体/人脸核验（FR-135）数据库脚本（p2p_consumer 库）
-- 先执行 xfjr-init.sql 建 p2p_consumer 库，再执行本文件；IF NOT EXISTS 幂等
-- 仅存分数与结果，不存原始人脸图像（AC4）

USE `p2p_consumer`;

-- 用户实名信息（含活体/人脸字段）
CREATE TABLE IF NOT EXISTS `consumer_realname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumer_no` varchar(64) NOT NULL COMMENT '用户编码',
  `real_name` varchar(64) DEFAULT NULL COMMENT '姓名',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `status` varchar(16) DEFAULT 'UNVERIFIED' COMMENT 'UNVERIFIED/VERIFIED/REVIEW/REJECTED',
  `liveness_score` decimal(5,4) DEFAULT NULL COMMENT '活体检测分数(0~1)',
  `face_match_score` decimal(5,4) DEFAULT NULL COMMENT '人脸比对分数(0~1)',
  `liveness_passed` tinyint(1) DEFAULT '0' COMMENT '活体是否通过 1是 0否',
  `risk_flag` tinyint(1) DEFAULT '0' COMMENT '风险标记 1是 0否',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consumer_no` (`consumer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户实名信息';

-- 活体/人脸核验记录（分数与结果可追溯）
CREATE TABLE IF NOT EXISTS `realname_liveness_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumer_no` varchar(64) NOT NULL COMMENT '用户编码',
  `liveness_score` decimal(5,4) DEFAULT NULL COMMENT '活体检测分数(0~1)',
  `face_match_score` decimal(5,4) DEFAULT NULL COMMENT '人脸比对分数(0~1)',
  `liveness_passed` tinyint(1) DEFAULT '0' COMMENT '活体是否通过 1是 0否',
  `attack_type` varchar(16) DEFAULT NULL COMMENT '攻击类型 PHOTO/REPLAY/SCREEN/MASK',
  `status` varchar(16) DEFAULT NULL COMMENT 'PASSED/REVIEW/REJECTED',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `message` varchar(255) DEFAULT NULL COMMENT '结果说明',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_consumer_no` (`consumer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='活体/人脸核验记录';
