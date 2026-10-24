-- 会员与积分（FR-127）数据库脚本（p2p_consumer 库）
-- 运行顺序：先执行 docs/sql/p2p_sql/xfjr-init.sql 建 p2p_consumer 库，再执行本文件
-- 使用 IF NOT EXISTS，可重复执行幂等（不覆盖已有数据）

USE `p2p_consumer`;

-- 会员等级配置（等级门槛 + 权益配置）
CREATE TABLE IF NOT EXISTS `member_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level_code` varchar(32) NOT NULL COMMENT '等级编码 BRONZE/SILVER/GOLD/PLATINUM',
  `level_name` varchar(64) DEFAULT NULL COMMENT '等级名称',
  `level_no` int(11) DEFAULT '1' COMMENT '等级序号，越大越高',
  `min_lend_amount` decimal(18,2) DEFAULT '0.00' COMMENT '累计出借金额门槛',
  `privilege` varchar(255) DEFAULT NULL COMMENT '权益描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='会员等级配置';

-- 用户会员档案（当前等级 + 累计出借 + 活跃度）
CREATE TABLE IF NOT EXISTS `member_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumer_no` varchar(64) NOT NULL COMMENT '用户编码',
  `current_level` varchar(32) DEFAULT NULL COMMENT '当前等级编码',
  `cumulative_lend_amount` decimal(18,2) DEFAULT '0.00' COMMENT '累计出借金额',
  `active_score` int(11) DEFAULT '0' COMMENT '活跃度',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consumer_no` (`consumer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户会员档案';

-- 等级变更历史（FR-127 AC1）
CREATE TABLE IF NOT EXISTS `member_level_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumer_no` varchar(64) NOT NULL COMMENT '用户编码',
  `from_level` varchar(32) DEFAULT NULL COMMENT '原等级',
  `to_level` varchar(32) DEFAULT NULL COMMENT '新等级',
  `change_reason` varchar(255) DEFAULT NULL COMMENT '变更原因',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_consumer_no` (`consumer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='等级变更历史';

-- 积分流水（FR-127 AC2/AC3）
CREATE TABLE IF NOT EXISTS `member_point_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `consumer_no` varchar(64) NOT NULL COMMENT '用户编码',
  `point_type` varchar(16) NOT NULL COMMENT 'EARN-获取 CONSUME-消耗',
  `biz_type` varchar(16) DEFAULT NULL COMMENT 'LOAN/REPAY/INVITE/DEDUCT/EXCHANGE',
  `amount` bigint(20) NOT NULL COMMENT '积分变动量(正数)',
  `balance_after` bigint(20) DEFAULT NULL COMMENT '变动后余额',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '业务流水号(幂等键)',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_consumer_no` (`consumer_no`),
  UNIQUE KEY `uk_consumer_bizno` (`consumer_no`,`biz_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='积分流水';

-- 初始化等级配置（幂等：唯一键 + INSERT IGNORE）
INSERT IGNORE INTO `member_level` (`level_code`,`level_name`,`level_no`,`min_lend_amount`,`privilege`,`status`,`create_date`) VALUES
 ('BRONZE','青铜会员',1,0.00,'基础权益',1,NOW()),
 ('SILVER','白银会员',2,100000.00,'提升额度、积分加速',1,NOW()),
 ('GOLD','黄金会员',3,500000.00,'专属客服、费率优惠',1,NOW()),
 ('PLATINUM','铂金会员',4,1000000.00,'最高权益、优先审核',1,NOW());
