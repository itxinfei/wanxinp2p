-- 认证授权体系（FR-129）数据库脚本（p2p_uaa 库）
-- 通过 JDBC 客户端仓库（oauth_client_details.authorized_grant_types）启用令牌签发模式：
--   授权码+PKCE（authorization_code）、客户端凭证（client_credentials）、刷新令牌（refresh_token）
-- 幂等：仅在未包含对应授权类型时追加，重复执行安全

USE `p2p_uaa`;

-- 服务间客户端：启用 client_credentials（服务间调用使用独立凭证令牌，FR-130 AC5）
UPDATE `oauth_client_details`
   SET `authorized_grant_types` = CONCAT(`authorized_grant_types`, ',client_credentials')
 WHERE `client_id` = 'xfjr-internal'
   AND (`authorized_grant_types` IS NULL OR `authorized_grant_types` NOT LIKE '%client_credentials%');

-- 前端客户端：启用 refresh_token（刷新令牌）
UPDATE `oauth_client_details`
   SET `authorized_grant_types` = CONCAT(`authorized_grant_types`, ',refresh_token')
 WHERE `client_id` IN ('xfjr-web-admin', 'xfjr-web-h5')
   AND (`authorized_grant_types` IS NULL OR `authorized_grant_types` NOT LIKE '%refresh_token%');

-- 前端客户端：启用 authorization_code（授权码+PKCE，推荐模式）
UPDATE `oauth_client_details`
   SET `authorized_grant_types` = CONCAT(`authorized_grant_types`, ',authorization_code')
 WHERE `client_id` IN ('xfjr-web-admin', 'xfjr-web-h5')
   AND (`authorized_grant_types` IS NULL OR `authorized_grant_types` NOT LIKE '%authorization_code%');

-- 为启用 authorization_code 的客户端补充重定向地址（如为空）
UPDATE `oauth_client_details`
   SET `web_server_redirect_uri` = 'https://admin.xfjr.local/callback'
 WHERE `client_id` = 'xfjr-web-admin'
   AND (`web_server_redirect_uri` IS NULL OR `web_server_redirect_uri` = '');
