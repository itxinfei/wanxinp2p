-- =============================================================================
-- 迁移：OAuth2 客户端由 InMemory / 旧 oauth_client_details 切换为 SAS JDBC（I-02 / FR-129）
-- 适用：已部署的 p2p_uaa 库（xfjr-init.sql 重建库的场景无需执行本脚本）
-- 执行后：xfjr-uaa 首次启动时由 RegisteredClientSeedRunner 写入三个客户端，
--         密钥为 BCrypt 密文；表中不保留明文。
-- 回滚：还原旧表结构与代码中的 InMemoryRegisteredClientRepository 即可。
-- =============================================================================

USE `p2p_uaa`;

DROP TABLE IF EXISTS `oauth_client_details`;
DROP TABLE IF EXISTS `oauth2_registered_client`;

CREATE TABLE `oauth2_registered_client` (
  `id` varchar(100) NOT NULL,
  `client_id` varchar(100) NOT NULL,
  `client_id_issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `client_secret` varchar(200) DEFAULT NULL,
  `client_secret_expires_at` timestamp NULL DEFAULT NULL,
  `client_name` varchar(200) DEFAULT NULL,
  `client_authentication_methods` varchar(1000) NOT NULL,
  `authorization_grant_types` varchar(1000) NOT NULL,
  `redirect_uris` varchar(1000) DEFAULT NULL,
  `post_logout_redirect_uris` varchar(1000) DEFAULT NULL,
  `scopes` varchar(1000) NOT NULL,
  `client_settings` varchar(2000) NOT NULL,
  `token_settings` varchar(2000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
