<h1 align="center">心飞金融 · xfjr</h1>

<h4 align="center">理财 · 借贷 · 风控 · 存管 —— 一套完整的互联网金融演示系统</h4>

<p align="center">
  <a href="https://gitee.com/itxinfei/xfjr/stargazers"><img src="https://gitee.com/itxinfei/xfjr/badge/star.svg?theme=gvp" alt="star"></a>
  <a href="https://gitee.com/itxinfei/xfjr/members"><img src="https://gitee.com/itxinfei/xfjr/badge/fork.svg?theme=gvp" alt="fork"></a>
  <img src="https://img.shields.io/badge/purpose-learning%20demo-lightgrey.svg" alt="purpose">
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="license">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-25-ED8B00.svg?logo=openjdk&logoColor=white" alt="JDK">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.3-6DB33F.svg?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.1.4-6DB33F.svg?logo=spring&logoColor=white" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/ShardingSphere-5.5.3-2B6CB0.svg" alt="ShardingSphere">
  <img src="https://img.shields.io/badge/Seata-2.6-2B6CB0.svg" alt="Seata">
  <img src="https://img.shields.io/badge/RocketMQ-2.3.6-D77310.svg?logo=apacherocketmq&logoColor=white" alt="RocketMQ">
  <img src="https://img.shields.io/badge/Apollo-2.5-red.svg" alt="Apollo">
  <img src="https://img.shields.io/badge/Elasticsearch-9.5.7-FEC514.svg?logo=elasticsearch&logoColor=white" alt="ES">
  <img src="https://img.shields.io/badge/MySQL-8.4-4479A1.svg?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Vue-3-42b883.svg?logo=vue.js&logoColor=white" alt="Vue">
</p>

<p align="center">
  <a href="#一这是什么">这是什么</a> ·
  <a href="#二这个项目在练什么">练什么</a> ·
  <a href="#三功能域">功能域</a> ·
  <a href="#五技术选型">技术选型</a> ·
  <a href="#七快速开始">快速开始</a> ·
  <a href="#八容易踩的坑">踩坑</a> ·
  <a href="https://gitee.com/itxinfei/xfjr/releases">版本发布</a>
</p>

---

> 声明：本项目是学习用的演示系统，不接入真实资金，不提供任何金融产品。P2P 网贷业务在国内已于 2020 年整体清退，此处的"出借""投标"等概念仅用于还原技术场景，不具备现实业务意义。

## 一、这是什么

理财、借贷、风控、存管四个域凑成一套互联网金融演示系统。技术上练的是分布式事务、分库分表、服务编排这几件事；业务上还原的是那个已经退场的 P2P 时代。

## 二、这个项目在练什么

金融系统难的不是增删改查，是这四件事：

1. **一笔钱不能同时进两个户** —— Seata AT 模式 + RocketMQ 事务消息双保险
2. **交易数据量长得快** —— ShardingSphere 按用户 ID 把交易库水平拆成两个
3. **多个服务要同时改状态** —— 状态机驱动，不许越过节点
4. **对账必须平** —— 存管代理独立成服务，资金流水和业务流水分开记，差异单独校验

这四条构成了项目的骨架，其余都是围绕它们的业务包装。

## 三、功能域

| 域 | 内容 |
|---|---|
| 统一认证 | OAuth2 授权码模式 + JWT，密码与短信验证码登录 |
| 统一账户 | 开户、绑卡、余额、信用等级 |
| 用户中心 | 个人信息、实名认证（百度 AI）、资产总览 |
| 交易中心 | 标的管理、出借投标、满标放款 |
| 还款服务 | 还款计划生成、按期还款、逾期处理 |
| 存管代理 | 与银行存管系统做数据交互 |
| 全文检索 | Elasticsearch 多条件标的检索 |

## 四、服务与端口

| 服务 | 模块 | 端口 |
|---|---|---|
| 注册中心 | `xfjr-discover-server` | 53000 |
| 网关 | `xfjr-gateway-server` | 53010 |
| 统一认证 | `xfjr-uaa` | 53020 |
| 统一账户 | `xfjr-account` | 53030 |
| 用户中心 | `xfjr-consumer` | 53050 |
| 交易中心 | `xfjr-transaction` | 53060 |
| 存管代理 | `xfjr-depository-agent` | 53070 |
| 还款服务 | `xfjr-repayment` | 53080 |
| 检索服务 | `xfjr-content-search` | 54081 |
| 银行存管（模拟，外置独立构建） | `xfjr-depository` | 55010 |

公共模块：`xfjr-common`、`xfjr-api`。

## 五、技术选型

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

## 六、数据库

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

## 七、快速开始

环境：JDK 25、Maven 3.9+、MySQL 8.0+、Redis 6+、RocketMQ 5.x、Apollo 2.x、Elasticsearch 7.x+。

1. 建库
2. 起 Apollo 并导入配置
3. 编译
4. 按顺序启动

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
java -jar xfjr-uaa/target/xfjr-uaa.jar -Denv=dev -Dserver.port=53020
java -jar xfjr-account/target/xfjr-account.jar
java -jar xfjr-gateway-server/target/xfjr-gateway-server.jar -Denv=dev -Dserver.port=53010
```

前端：

```bash
cd xfjr-manager && npm install && npm run dev    # 管理后台
cd xfjr-front-h5                                   # uni-app，用 HBuilderX 打开
```

## 八、容易踩的坑

- **配置全在 Apollo** —— 不先导入配置，服务起不来，报的错还跟配置无关，容易绕远路
- **启动顺序** —— 注册中心 → 认证 → 业务服务 → 网关，顺序错了网关会一直 503
- **JDK 23+ 要开注解处理** —— `-proc:full`，父 POM 里已经配好，自己拆模块时注意别丢
- **`xfjr-depository` 是外置模拟系统** —— 外部银行存管系统的模拟，已从主工程聚合中移出（根 pom 不含该 module），需先 `mvn install -pl xfjr-common` 再 `mvn install -f xfjr-depository/pom.xml` 独立构建；不注册业务 Eureka，只被存管代理经 `depository.url` 访问。详见 `docs/存管系统说明.md`
- **日志与健康检查** —— 日志在 `logs/`，健康检查走 `/actuator/health`

## 九、工程结构

```
xfjr/
├── xfjr-discover-server            注册中心
├── xfjr-gateway-server             网关
├── xfjr-uaa                统一认证
├── xfjr-account            统一账户
├── xfjr-consumer           用户中心
├── xfjr-transaction        交易中心
├── xfjr-repayment          还款
├── xfjr-depository-agent   存管代理
├── xfjr-content-search     检索
├── xfjr-depository                 存管模拟
├── xfjr-common / xfjr-api          公共组件与接口定义
├── xfjr-manager                    管理后台（Vue 3 + Element Plus + Vite）
├── xfjr-front-h5                   H5（uni-app）
└── docs/                           需求文档 v4.2、重构规划 v1.2、SQL、架构图
```

## 十、开源协议

Apache-2.0，见 [LICENSE](LICENSE) 与 [NOTICE](NOTICE)。

## 十一、交流

作者：心飞为你飞（itxinfei）

<p align="center">
  <a href="https://qm.qq.com/cgi-bin/qm/qr?k=0&group_code=661543188"><img src="https://img.shields.io/badge/QQ%E7%BE%A4-661543188-blue.svg?logo=tencentqq&logoColor=white" alt="QQ群"></a>
  <a href="mailto:747011882@qq.com"><img src="https://img.shields.io/badge/email-747011882%40qq.com-orange.svg" alt="email"></a>
  <a href="https://gitee.com/itxinfei"><img src="https://img.shields.io/badge/Gitee-itxinfei-C71D23.svg?logo=gitee&logoColor=white" alt="gitee"></a>
</p>
