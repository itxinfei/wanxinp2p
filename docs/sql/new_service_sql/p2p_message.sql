-- 消息服务数据库初始化脚本（p2p_message）
-- 运行顺序建议：先执行 docs/sql/p2p_sql/xfjr-init.sql，再执行本文件
-- 本文件 CREATE DATABASE 使用 IF NOT EXISTS，可重复执行幂等

CREATE DATABASE IF NOT EXISTS `p2p_message` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `p2p_message`;

-- 消息模板
DROP TABLE IF EXISTS `notification_template`;
CREATE TABLE `notification_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) DEFAULT NULL COMMENT '模板名称',
  `channel` varchar(16) DEFAULT NULL COMMENT 'SMS-短信 STATION-站内信 APP-推送',
  `content` varchar(512) DEFAULT NULL COMMENT '模板内容(含{占位符})',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息模板';

-- 消息通知记录
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notify_no` varchar(64) DEFAULT NULL COMMENT '通知编号',
  `template_code` varchar(64) DEFAULT NULL COMMENT '模板编码',
  `channel` varchar(16) DEFAULT NULL COMMENT '渠道',
  `receiver` varchar(64) DEFAULT NULL COMMENT '接收人(手机号/用户号)',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` varchar(1024) DEFAULT NULL COMMENT '内容',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务号',
  `send_status` tinyint(1) DEFAULT '0' COMMENT '0待发 1成功 2失败',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '失败原因',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver`),
  KEY `idx_biz_no` (`biz_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息通知记录';
