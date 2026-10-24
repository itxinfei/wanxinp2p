-- 电子合同与存证（FR-133）数据库脚本（p2p_consumer 库）
-- 先执行 xfjr-init.sql 建 p2p_consumer 库，再执行本文件；IF NOT EXISTS 幂等

USE `p2p_consumer`;

-- 合同模板
CREATE TABLE IF NOT EXISTS `contract_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_no` varchar(64) NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) DEFAULT NULL COMMENT '模板名称',
  `contract_type` varchar(16) NOT NULL COMMENT '适用合同类型 LOAN/BROKERAGE',
  `template_content` text COMMENT '模板内容(含${}占位符)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_no` (`template_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='合同模板';

-- 电子合同
CREATE TABLE IF NOT EXISTS `contract` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_no` varchar(64) NOT NULL COMMENT '合同编号',
  `type` varchar(16) NOT NULL COMMENT 'LOAN-借款合同 BROKERAGE-居间服务协议',
  `project_id` bigint(20) DEFAULT NULL COMMENT '项目标识',
  `lender_id` bigint(20) DEFAULT NULL COMMENT '出借人标识',
  `borrower_id` bigint(20) DEFAULT NULL COMMENT '借款人标识',
  `template_id` bigint(20) DEFAULT NULL COMMENT '模板标识',
  `file_id` varchar(64) DEFAULT NULL COMMENT '文件服务存储标识',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件哈希SHA-256(防篡改)',
  `content` text COMMENT '渲染后合同内容',
  `status` varchar(16) DEFAULT 'GENERATED' COMMENT 'GENERATED/SIGNED/EVIDENCED/ARCHIVED/EXPIRED',
  `sign_time` datetime DEFAULT NULL COMMENT '签署时间',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_lender` (`lender_id`),
  KEY `idx_borrower` (`borrower_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='电子合同';

-- 合同签署记录
CREATE TABLE IF NOT EXISTS `contract_sign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_no` varchar(64) NOT NULL COMMENT '合同编号',
  `signer` varchar(64) DEFAULT NULL COMMENT '签署人',
  `sign_type` varchar(32) DEFAULT NULL COMMENT '签署方式 SMS_VERIFY/CA/IMAGE',
  `cert_serial` varchar(128) DEFAULT NULL COMMENT '证书序列号',
  `sign_time` datetime DEFAULT NULL COMMENT '签署时间',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_no` (`contract_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='合同签署记录';

-- 合同存证记录
CREATE TABLE IF NOT EXISTS `contract_evidence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_no` varchar(64) NOT NULL COMMENT '合同编号',
  `evidence_type` varchar(16) DEFAULT NULL COMMENT 'THIRD_PARTY/CHAIN/DOUBLE_RECORD',
  `evidence_no` varchar(128) DEFAULT NULL COMMENT '存证编号',
  `storage_url` varchar(512) DEFAULT NULL COMMENT '存证存储地址',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_no` (`contract_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='合同存证记录';

-- 初始化合同模板（幂等）
INSERT IGNORE INTO `contract_template` (`template_no`,`template_name`,`contract_type`,`template_content`,`status`,`create_date`) VALUES
 ('TPL_LOAN_001','借款合同模板','LOAN','借款合同\n项目：${projectId}\n出借人：${lenderId}\n借款人：${borrowerId}\n签署日期：${signDate}\n（正式模板内容）',1,NOW()),
 ('TPL_BROKERAGE_001','居间服务协议模板','BROKERAGE','居间服务协议\n项目：${projectId}\n借款人：${borrowerId}\n签署日期：${signDate}\n（正式模板内容）',1,NOW());
