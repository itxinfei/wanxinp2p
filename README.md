<div align="center">

# 心飞金融（xfjr）

理财 · 借贷 · 风控 · 存管，一套完整的互联网金融演示系统

<img alt="JDK" src="https://img.shields.io/badge/JDK-25-brightgreen">
<img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen">
<img alt="Spring Cloud" src="https://img.shields.io/badge/Spring%20Cloud-2025.1.3-orange">
<img alt="RocketMQ" src="https://img.shields.io/badge/RocketMQ-2.3.6-red">
<img alt="Seata" src="https://img.shields.io/badge/Seata-2.6.0-red">
<img alt="ShardingSphere" src="https://img.shields.io/badge/ShardingSphere-5.5.3-red">
<img alt="Apollo" src="https://img.shields.io/badge/Apollo-2.5.0-red">

</div>

> **声明**：本项目是学习用的演示系统，不接入真实资金，不提供任何金融产品。P2P 网贷业务在国内已于 2020 年整体清退，此处的"出借""投标"等概念仅用于还原技术场景，不具备现实业务意义。

## 这个项目在练什么

金融系统难的不是增删改查，是这四件事：

1. **一笔钱不能同时进两个户** —— Seata AT 模式 + RocketMQ 事务消息双保险
2. **交易数据量长得快** —— ShardingSphere 按用户 ID 把交易库水平拆成两个
3. **多个服务要同时改状态** —— 状态机驱动，不许越过节点
4. **对账必须平** —— 存管代理独立成服务，资金流水和业务流水分开记，差异单独校验

这四条构成了项目的骨架，其余都是围绕它们的业务包装。

## 功能域

| 域 | 内容 |
|---|---|
| 统一认证 | OAuth2 授权码模式 + JWT，密码与短信验证码登录 |
| 统一账户 | 开户、绑卡、余额、信用等级 |
| 用户中心 | 个人信息、实名认证（百度 AI）、资产总览 |
| 交易中心 | 标的管理、出借投标、满标放款 |
| 还款服务 | 还款计划生成、按期还款、逾期处理 |
| 存管代理 | 与银行存管系统做数据交互 |
| 全文检索 | Elasticsearch 多条件标的检索 |

## 服务与端口

| 服务 | 模块 | 端口 |
|---|---|---|
| 注册中心 | `xfjr-discover-server` | 53000 |
| 网关 | `xfjr-gateway-server` | 53010 |
| 统一认证 | `xfjr-uaa-service` | 53020 |
| 统一账户 | `xfjr-account-service` | 53030 |
| 用户中心 | `xfjr-consumer-service` | 53050 |
| 交易中心 | `xfjr-transaction-service` | 53060 |
| 存管代理 | `xfjr-depository-agent-service` | 53070 |
| 还款服务 | `xfjr-repayment-service` | 53080 |
| 检索服务 | `xfjr-content-search-service` | 54081 |
| 银行存管（模拟） | `xfjr-depository` | 55010 |

公共模块：`xfjr-common`、`xfjr-api`。

## 技术选型

| 层 | 选型 |
|---|---|
| 基础 | JDK 25 + Spring Boot 4.1.1 + Spring Cloud 2025.1.3 |
| 注册发现 | Eureka |
| 网关 | Spring Cloud Gateway + Sentinel 1.8.10（规则存 Apollo 动态推送） |
| 认证 | Spring Authorization Server 1.5 + JWT |
| 持久层 | MyBatis-Plus 3.5.17 + Druid + MySQL 8.4 |
| 分库分表 | ShardingSphere 5.5.3 JDBC |
| 消息 | RocketMQ 2.3.6（事务消息 + 延时消息 + 死信队列） |
| 分布式事务 | Seata 2.6（AT 模式） |
| 调度 | `@Scheduled` + ShedLock 7.10 |
| 配置 | Apollo 2.5 |
| 检索 | Elasticsearch |
| 文档 | springdoc-openapi 3.1.1 |
| 序列化 | FastJSON2 2.0.66 + Jackson |
| 前端 | 管理后台 Vue 3 + Element Plus + Vite；移动端 uni-app |

架构图上没有画，因为画了也只是一个服务列表的复述——真正的信息是上面那张表和下面的库表拆分，看图不如看表。

## 数据库

| 库 | 内容 |
|---|---|
| `p2p_uaa` | OAuth2 客户端、用户凭证 |
| `p2p_account` | 余额、信用等级、绑定银行卡 |
| `p2p_consumer` | 个人信息、实名认证 |
| `p2p_transaction_0/1` | 交易数据，ShardingSphere 水平拆两库 |
| `p2p_repayment` | 还款计划、逾期记录 |
| `p2p_bank_depository` | 存管资金流水 |
| `p2p_depository_agent` | 存管代理数据 |
| `p2p_reconciliation` | 对账数据 |
| `p2p_undo_log` | Seata 回滚日志 |

## 跑起来

环境：JDK 25、Maven 3.9+、MySQL 8.0+、Redis 6+、RocketMQ 5.x、Apollo 2.x、Elasticsearch 7.x+。

```bash
# 1. 建库
mysql -u root -p < docs/p2p_sql/p2p_account.sql
mysql -u root -p < docs/p2p_sql/p2p_transaction_0.sql
# 其余脚本见 docs/p2p_sql/

# 2. 起 Apollo，导入 docs/Apollo_sql/p2p_apollo.sql

# 3. 编译
mvn clean install -DskipTests

# 4. 按顺序启动
java -jar xfjr-discover-server/target/xfjr-discover-server.jar
java -jar xfjr-uaa-service/target/xfjr-uaa-service.jar -Denv=dev -Dserver.port=53020
java -jar xfjr-account-service/target/xfjr-account-service.jar
java -jar xfjr-gateway-server/target/xfjr-gateway-server.jar -Denv=dev -Dserver.port=53010
```

前端：

```bash
cd xfjr-manager && npm install && npm run dev    # 管理后台
cd xfjr-front-h5                                   # uni-app，用 HBuilderX 打开
```

## 几个容易踩的坑

- **配置全在 Apollo**。不先导入配置，服务起不来，报的错还跟配置无关，容易绕远路。
- **启动顺序**：注册中心 → 认证 → 业务服务 → 网关。顺序错了网关会一直 503。
- **JDK 23+ 要开注解处理**：`-proc:full`，父 POM 里已经配好，自己拆模块时注意别丢。
- **`xfjr-depository` 是模拟系统**，包名 `com.xfjr.depository`，主类 `XfjrDepositoryServer`，可以单独部署，不影响主链路。
- 日志在 `logs/`，健康检查走 `/actuator/health`。

## 工程结构

```
xfjr-discover-server        注册中心
xfjr-gateway-server         网关
xfjr-uaa-service            统一认证
xfjr-account-service        统一账户
xfjr-consumer-service       用户中心
xfjr-transaction-service    交易中心
xfjr-repayment-service      还款
xfjr-depository-agent-service  存管代理
xfjr-content-search-service 检索
xfjr-depository             存管模拟
xfjr-common / xfjr-api      公共组件与接口定义
xfjr-manager                管理后台（Vue 3 + Element Plus + Vite）
xfjr-front-h5               H5（uni-app）
docs/                       需求文档 v4.2、重构规划 v1.2、SQL、架构图
```

## 许可

Apache-2.0，见 [LICENSE](LICENSE) 与 [NOTICE](NOTICE)。

## 联系

QQ 747011882 ｜ 邮箱 747011882@qq.com
