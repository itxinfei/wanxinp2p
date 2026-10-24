-- 风控服务数据库初始化脚本（p2p_risk）
-- 运行顺序建议：先执行 docs/sql/p2p_sql/xfjr-init.sql，再执行本文件
-- 本文件 CREATE DATABASE 使用 IF NOT EXISTS，可重复执行幂等

CREATE DATABASE IF NOT EXISTS `p2p_risk` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `p2p_risk`;

-- 风控规则
DROP TABLE IF EXISTS `risk_rule`;
CREATE TABLE `risk_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(128) DEFAULT NULL COMMENT '规则名称',
  `rule_type` varchar(32) DEFAULT NULL COMMENT '类型：ANTI_FRAUD-反欺诈 CREDIT_SCORE-信用评分 LIST-名单 QUOTA-额度',
  `expression` varchar(512) DEFAULT NULL COMMENT '规则表达式/配置(JSON)',
  `risk_weight` int(11) DEFAULT '0' COMMENT '权重',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='风控规则';

-- 风控名单（黑/白名单）
DROP TABLE IF EXISTS `risk_list`;
CREATE TABLE `risk_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `list_type` varchar(16) NOT NULL COMMENT 'BLACK-黑名单 WHITE-白名单',
  `dimension` varchar(16) DEFAULT NULL COMMENT '维度：MOBILE-手机号 ID_CARD-身份证 DEVICE-设备 IP-IP',
  `value` varchar(128) NOT NULL COMMENT '名单值',
  `reason` varchar(255) DEFAULT NULL COMMENT '命中/加入原因',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1生效 0失效',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_list_dim_value` (`list_type`,`dimension`,`value`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='风控名单';

-- 风控决策记录
DROP TABLE IF EXISTS `risk_decision`;
CREATE TABLE `risk_decision` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_no` varchar(64) NOT NULL COMMENT '业务流水号(REQUEST_NO)',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型：REGISTER-注册 OPEN_ACCOUNT-开户 RECHARGE-充值 WITHDRAW-提现 ISSUE_LOAN-发标',
  `consumer_no` varchar(64) DEFAULT NULL COMMENT '用户编号',
  `rule_hits` varchar(1024) DEFAULT NULL COMMENT '命中规则编码(逗号分隔)',
  `decision` varchar(16) DEFAULT NULL COMMENT 'PASS-通过 REJECT-拒绝 REVIEW-人工',
  `score` int(11) DEFAULT NULL COMMENT '信用评分',
  `detail` text COMMENT '决策详情(JSON)',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz_no` (`biz_no`),
  KEY `idx_consumer_no` (`consumer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='风控决策记录';
