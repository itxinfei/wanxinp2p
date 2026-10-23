-- 对账服务数据库初始化脚本（p2p_reconciliation）
-- 运行顺序建议：先执行 docs/sql/p2p_sql/xfjr-init.sql（其已创建 p2p_reconciliation 库），再执行本文件
-- 若独立运行，本文件 CREATE DATABASE 使用 IF NOT EXISTS，幂等安全

CREATE DATABASE IF NOT EXISTS `p2p_reconciliation` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `p2p_reconciliation`;

-- 对账批次
DROP TABLE IF EXISTS `reconciliation_batch`;
CREATE TABLE `reconciliation_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `bill_date` date DEFAULT NULL COMMENT '对账日期',
  `channel` varchar(32) DEFAULT NULL COMMENT '渠道 bank-depository',
  `total_count` int(11) DEFAULT NULL COMMENT '平台笔数',
  `total_amount` decimal(18,6) DEFAULT NULL COMMENT '平台金额',
  `bank_count` int(11) DEFAULT NULL COMMENT '银行笔数',
  `bank_amount` decimal(18,6) DEFAULT NULL COMMENT '银行金额',
  `diff_count` int(11) DEFAULT '0' COMMENT '差异笔数',
  `status` tinyint(1) DEFAULT '0' COMMENT '0初始 1平账 2有差异',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='对账批次';

-- 对账明细
DROP TABLE IF EXISTS `reconciliation_detail`;
CREATE TABLE `reconciliation_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `biz_no` varchar(64) NOT NULL COMMENT '业务流水号',
  `direction` varchar(8) DEFAULT NULL COMMENT 'IN-入金 OUT-出金',
  `amount` decimal(18,6) DEFAULT NULL COMMENT '金额',
  `status` varchar(16) DEFAULT NULL COMMENT 'PLATFORM-仅平台 BANK-仅银行 BOTH-双方一致 DIFF-不一致',
  `platform_status` varchar(16) DEFAULT NULL COMMENT '平台侧状态',
  `bank_status` varchar(16) DEFAULT NULL COMMENT '银行侧状态',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_biz` (`batch_no`,`biz_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='对账明细';

-- 对账差异
DROP TABLE IF EXISTS `reconciliation_diff`;
CREATE TABLE `reconciliation_diff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '业务流水号',
  `diff_type` varchar(16) DEFAULT NULL COMMENT 'LONG-长款 SHORT-短款 AMOUNT-金额不一致 STATUS-状态不一致',
  `platform_amount` decimal(18,6) DEFAULT NULL COMMENT '平台金额',
  `bank_amount` decimal(18,6) DEFAULT NULL COMMENT '银行金额',
  `handle_status` tinyint(1) DEFAULT '0' COMMENT '0未处理 1已处理',
  `handle_type` varchar(16) DEFAULT NULL COMMENT 'REPAIR-补单 HANG-挂账 REGISTER-登账 ADJUST-调账',
  `handle_remark` varchar(255) DEFAULT NULL COMMENT '处理备注',
  `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='对账差异';
