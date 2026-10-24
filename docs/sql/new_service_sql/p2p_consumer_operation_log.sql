-- 操作日志与审计（FR-132）数据库脚本（p2p_consumer 库）
-- 追加写 + 哈希链，不可篡改（AC2）

USE `p2p_consumer`;

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `action` varchar(64) DEFAULT NULL COMMENT '动作类型',
  `target` varchar(128) DEFAULT NULL COMMENT '操作对象',
  `result` varchar(16) DEFAULT NULL COMMENT '结果 SUCCESS/FAIL',
  `ip` varchar(64) DEFAULT NULL COMMENT '来源IP',
  `prev_hash` varchar(64) DEFAULT NULL COMMENT '上一条哈希(创世为GENESIS)',
  `cur_hash` varchar(64) DEFAULT NULL COMMENT '本条哈希',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator`),
  KEY `idx_action` (`action`),
  KEY `idx_create_date` (`create_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='操作日志(仅追加,哈希链)';
