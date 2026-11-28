-- =============================================================================
-- Apollo 种子数据修正（I-17 收尾）：针对 p2p_apollo.sql 导入后的 apolloconfigdb
-- 目标：清理旧命名前缀、拼写错误的过期技术项与历史公网地址。
-- 适用范围：Apollo 3.x（表名 ConfigurationValue，库 apolloconfigdb）。
--   2.x 旧版表名相同，若实际库名不同请先调整。
-- 注意：只改"当前生效配置"（ConfigurationValue）；Release/Item/Audit 等历史快照保持原样，
--       保留审计痕迹。改完需在 Apollo 控制台重新发布对应命名空间才生效。
-- 回滚：见文件末尾的反向语句（按注释恢复原值）。
-- =============================================================================

USE `apolloconfigdb`;

-- 1) 过期技术项：elasticjob 早已被 shedlock 取代（键名本身拼写错误 elaticjob.*）
DELETE FROM `ConfigurationValue`
WHERE `Key` IN ('elaticjob.zookeeper.server-lists', 'elaticjob.zookeeper.namespace');

-- 2) RocketMQ producer group 旧前缀（xfjr-transaction 命名空间当前值）
UPDATE `ConfigurationValue`
SET `Value` = 'xfjr-transaction'
WHERE `Key` = 'rocketmq.producer.group'
  AND `Value` = 'xinfei_p2p_transaction';

-- 3) 历史公网地址回收为本地开发地址（其余库均为 localhost:3306）
UPDATE `ConfigurationValue`
SET `Value` = REPLACE(`Value`, '218.29.75.133:6306', 'localhost:3306')
WHERE `Key` = 'spring.datasource.url'
  AND `Value` LIKE '%218.29.75.133%';

UPDATE `ConfigurationValue`
SET `Value` = REPLACE(`Value`, '211.103.136.242:7137', 'localhost:3306')
WHERE `Key` IN ('spring.datasource.url', 'spring.shardingsphere.datasource.ds0.url',
                'spring.shardingsphere.datasource.ds1.url')
  AND `Value` LIKE '%211.103.136.242%';

UPDATE `ConfigurationValue`
SET `Value` = REPLACE(`Value`, '211.103.136.242:3306', 'localhost:3306')
WHERE `Key` LIKE 'spring.shardingsphere.datasource.ds%.url'
  AND `Value` LIKE '%211.103.136.242%';

-- 4) RocketMQ name-server 统一为本机（与其余服务一致）
UPDATE `ConfigurationValue`
SET `Value` = '127.0.0.1:9876'
WHERE `Key` IN ('rocketmq.name-server', 'rocketmq.nameServer')
  AND `Value` LIKE '%:%' AND `Value` <> '127.0.0.1:9876';

-- 5) 存管回调地址旧域名（若有）改为本地网关
UPDATE `ConfigurationValue`
SET `Value` = 'http://localhost:53070/'
WHERE `Key` LIKE 'client-info.clients[%].notifyUrl'
  AND `Value` LIKE '%xinfei.bb.cn%';

-- -----------------------------------------------------------------------------
-- 回滚（仅在需要恢复旧值时手工执行，按需取用）
-- -----------------------------------------------------------------------------
-- UPDATE `ConfigurationValue` SET `Value`='xinfei_p2p_transaction'
--   WHERE `Key`='rocketmq.producer.group' AND `Value`='xfjr-transaction';
-- UPDATE `ConfigurationValue` SET `Value`=REPLACE(`Value`,'localhost:3306','218.29.75.133:6306')
--   WHERE `Key`='spring.datasource.url' AND `Value` LIKE '%p2p_consumer%' AND `NamespaceId`=41;
