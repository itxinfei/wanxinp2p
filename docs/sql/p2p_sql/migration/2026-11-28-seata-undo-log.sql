-- =============================================================================
-- 迁移：Seata AT 模式回滚日志表 undo_log（I-04：资金操作纳入分布式事务）
-- 适用：所有启用了 seata-spring-boot-starter 且走 AT 代理数据源的业务库
--      （p2p_account / p2p_consumer / p2p_transaction / p2p_repayment /
--        p2p_depository_agent / p2p_bank_depository）。
--      无写操作的库（p2p_file / p2p_reconciliation / p2p_uaa）可不执行。
-- 执行时机：在各服务加 @GlobalTransactional 之前完成，否则首次分支注册会因缺表报错。
-- 回滚：DROP TABLE undo_log（仅当不再使用 AT 模式时）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `p2p_account`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';

CREATE TABLE IF NOT EXISTS `p2p_consumer`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';

CREATE TABLE IF NOT EXISTS `p2p_transaction`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';

CREATE TABLE IF NOT EXISTS `p2p_repayment`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';

CREATE TABLE IF NOT EXISTS `p2p_depository_agent`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';

CREATE TABLE IF NOT EXISTS `p2p_bank_depository`.`undo_log` (
  `branch_id` BIGINT       NOT NULL COMMENT '分支事务 id',
  `xid` VARCHAR(128)    NOT NULL COMMENT '全局事务 id',
  `context` VARCHAR(128)    NOT NULL COMMENT '序列化上下文',
  `rollback_info` LONGBLOB   NOT NULL COMMENT '回滚快照',
  `log_status` INT           NOT NULL COMMENT '0 正常，1 防御',
  `log_created` DATETIME(6)  NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 回滚日志';
