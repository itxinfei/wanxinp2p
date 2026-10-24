-- 管理后台 RBAC（FR-131）数据库脚本（p2p_consumer 库）
-- 启用既有六表结构；IF NOT EXISTS 幂等（不覆盖已有数据）
-- 说明：权威结构见 docs/sql/p2p_sql/xfjr-init.sql；此处保证 p2p_consumer 内存在同构表，
--       供 consumer-service 的 RBAC 端点使用

USE `p2p_consumer`;

CREATE TABLE IF NOT EXISTS `role` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `CODE` varchar(50) DEFAULT NULL COMMENT '角色编码',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='角色信息';

CREATE TABLE IF NOT EXISTS `privilege_group` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `PARENT_ID` bigint(20) DEFAULT NULL COMMENT '父id',
  `NAME` varchar(50) DEFAULT NULL COMMENT '权限组名称',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='权限组';

CREATE TABLE IF NOT EXISTS `privilege` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(50) DEFAULT NULL COMMENT '权限名称',
  `CODE` varchar(50) DEFAULT NULL COMMENT '权限编码',
  `PRIVILEGE_GROUP_ID` bigint(20) DEFAULT NULL COMMENT '所属权限组id',
  PRIMARY KEY (`ID`),
  KEY `FK_Reference_6` (`PRIVILEGE_GROUP_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='权限';

CREATE TABLE IF NOT EXISTS `menu` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `PARENT_ID` bigint(20) DEFAULT NULL COMMENT '父id',
  `TITLE` varchar(50) NOT NULL COMMENT '菜单标题',
  `URL` varchar(200) DEFAULT NULL COMMENT '链接url',
  `ICON` varchar(50) DEFAULT NULL COMMENT '图标',
  `SORT` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `COMMENT` varchar(200) DEFAULT NULL COMMENT '说明',
  `STATUS` int(11) NOT NULL DEFAULT '1' COMMENT '状态',
  `PRIVILEGE_CODE` varchar(50) DEFAULT NULL COMMENT '绑定权限',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='菜单';

CREATE TABLE IF NOT EXISTS `role_privilege` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ROLE_ID` bigint(20) DEFAULT NULL COMMENT '角色id',
  `PRIVILEGE_ID` bigint(20) DEFAULT NULL COMMENT '权限id',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='角色-权限关系';

CREATE TABLE IF NOT EXISTS `account_role` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ACCOUNT_ID` bigint(20) DEFAULT NULL COMMENT '账号标识',
  `ROLE_ID` bigint(20) DEFAULT NULL COMMENT '角色标识',
  PRIMARY KEY (`ID`),
  KEY `Fk_Reference_15` (`ACCOUNT_ID`),
  KEY `FK-Reference_16` (`ROLE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='账号-角色关系';

-- 示例权限与角色（幂等；供联调，生产可按需调整）
INSERT IGNORE INTO `privilege` (`NAME`,`CODE`) VALUES ('审核标的','project:approve');
INSERT IGNORE INTO `privilege` (`NAME`,`CODE`) VALUES ('放款','loan:execute');
INSERT IGNORE INTO `role` (`NAME`,`CODE`) VALUES ('运营管理员','admin');
