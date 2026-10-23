-- 文件服务数据库初始化脚本（p2p_file）
-- 运行顺序建议：先执行 docs/sql/p2p_sql/xfjr-init.sql（其已创建 p2p_file 库），再执行本文件
-- 若独立运行，本文件 CREATE DATABASE 使用 IF NOT EXISTS，幂等安全

CREATE DATABASE IF NOT EXISTS `p2p_file` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `p2p_file`;

-- 文件对象
DROP TABLE IF EXISTS `fileobject`;
CREATE TABLE `fileobject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_id` varchar(64) NOT NULL COMMENT '文件ID(返回给业务方)',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_type` varchar(64) DEFAULT NULL COMMENT 'MIME类型',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型 ID_CARD-身份证 CONTRACT-合同 VOUCHER-凭证',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务号',
  `storage_type` varchar(16) DEFAULT NULL COMMENT 'LOCAL-本地 MINIO OSS',
  `storage_path` varchar(512) DEFAULT NULL COMMENT '存储路径/KEY',
  `file_size` bigint(20) DEFAULT NULL COMMENT '字节数',
  `md5` varchar(64) DEFAULT NULL COMMENT '文件摘要',
  `url` varchar(512) DEFAULT NULL COMMENT '访问地址',
  `expire_time` datetime DEFAULT NULL COMMENT '过期下载时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1有效 0失效',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='文件对象';
