-- 统计分析服务（FR-128）数据库初始化脚本（p2p_statistics）
-- T+1 聚合仓库：由外部聚合任务计算后推送到本服务（POST /l/statistics/sync）
-- 本文件 CREATE DATABASE 使用 IF NOT EXISTS，可重复执行幂等

CREATE DATABASE IF NOT EXISTS `p2p_statistics` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `p2p_statistics`;

-- 经营看板日聚合
CREATE TABLE IF NOT EXISTS `stat_daily_overview` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `outstanding_balance` decimal(18,2) DEFAULT '0.00' COMMENT '在贷余额',
  `pending_repay` decimal(18,2) DEFAULT '0.00' COMMENT '待还金额',
  `today_transaction` decimal(18,2) DEFAULT '0.00' COMMENT '当日交易额',
  `today_transaction_count` int(11) DEFAULT '0' COMMENT '当日交易笔数',
  `new_user_count` int(11) DEFAULT '0' COMMENT '当日新增用户数',
  `total_lend_amount` decimal(18,2) DEFAULT '0.00' COMMENT '累计出借金额',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='经营看板日聚合';

-- 风险看板日聚合
CREATE TABLE IF NOT EXISTS `stat_risk` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `overdue_rate` decimal(8,4) DEFAULT '0.0000' COMMENT '逾期率(0~1)',
  `migration_rate` decimal(8,4) DEFAULT '0.0000' COMMENT '迁徙率(0~1)',
  `overdue_amount` decimal(18,2) DEFAULT '0.00' COMMENT '逾期金额',
  `aging_distribution` varchar(512) DEFAULT NULL COMMENT '账龄分布JSON',
  `five_class_distribution` varchar(512) DEFAULT NULL COMMENT '五级分类JSON',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='风险看板日聚合';
