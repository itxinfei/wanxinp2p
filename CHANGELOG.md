# 变更记录

按时间倒序记录对行为、配置或依赖有实际影响的变更；纯文案与措辞调整不列入。

## 2026-11-28 安全加固（关闭 I-01 / I-02）

### 鉴权
- **关闭 I-01**：网关不再把 JWT 拆成 `jsonToken` 经 query 参数透传，只校验令牌并放行；consumer / repayment / transaction / account / content-search / depository-agent / risk / reconciliation / file / message / statistics 共 14 个服务接入本地 JWT 校验（`/my`、`/m`、`/api` 需 `SCOPE_read`，`/l/**` 暂放行）。
- 新增 `xfjr-api` 的 `JwtUserContext`，统一从安全上下文的 `Jwt` 声明还原登录用户，替代各服务自写的 `TokenInterceptor`。
- **FR-130 AC5 骨架**：Feign 调用在 `xfjr.internal-client.enabled=true` 时自动携带内部客户端令牌（client_credentials，内存缓存），服务端 `/l/**` 同步收敛为需 `SCOPE_read`；默认关闭，client-secret 只允许 Apollo / 环境变量注入，不写死在代码库。

### 认证中心
- **关闭 I-02**：删除约 670 行从未启用的 JDBC 客户端死代码（7 个类）；注册客户端改用 SAS `JdbcRegisteredClientRepository`（表 `oauth2_registered_client`），密钥由 `{noop}` 明文改为 BCrypt，启动时幂等播种 admin / h5 / internal 三个客户端。
- 新增 `POST /logout`：access token 按 jti 写入 Redis 黑名单，可选连带吊销 refresh token；`/oauth2/revoke`、`/oauth2/introspect` 为 SAS 自带端点。
- JWT 签名密钥持久化到 `xfjr.jwt.key-path`（默认 `${user.home}/.xfjr/jwt-signing-key.json`），首次启动自动生成；服务重启不再让全部已签发 access token 失效。

### 修复与脚本
- xfjr-depository `application.yml` 删除重复的 `apollo.bootstrap` 键（Spring Boot 4 严格 YAML 解析下该服务无法启动）。
- 建表脚本切换为 `oauth2_registered_client`；新增迁移脚本 `docs/sql/p2p_sql/migration/2026-11-28-oauth-client-jdbc.sql`。
- `.gitignore` 补充忽略 `.cursor/`、`.trae/` 工具规则目录。

### 分布式事务（I-04 / I-05，进行中）
- `xfjr-api` 新增 `SeataFeignInterceptor`：Feign 出站透传 Seata XID（`TX_XID`），使下游 AT 分支挂到同一全局事务；未开启事务时不注入任何头。自动装配条件为 classpath 存在 Seata。
- 新增 undo_log 建表迁移脚本 `docs/sql/p2p_sql/migration/2026-11-28-seata-undo-log.sql`（覆盖 6 个业务库，执行时机须早于加注解）。
- `transaction.createTender`（投标）、`projectsApprovalStatus`（发标审批）、`loansApprovalStatus`（放款）纳入 `@GlobalTransactional(rollbackFor = Exception.class)`；Seata 注解 import 统一到 `org.apache.seata.*` 新坐标。
- 澄清 I-05：`cancelRegister` 本就有真实删除逻辑，AT 模式下回滚由 undo_log 自动完成，confirm/cancel 保留用于人工修复并加注说明。

### 验证
- `mvn clean install -DskipTests` 全量通过；uaa 18 例、xfjr-api 6 例、transaction 33 例、gateway 4 例等单测通过。
- xfjr-account、xfjr-depository 各 1 例失败为既有环境缺陷（Seata 分组配置缺失、MyBatis 装配），基线复跑确认与本次改动无关。

## 2026-11-25 基线升级
- JDK 25 与中间件镜像定版：ES 9.5.7 / Nacos 3.3.0 / Redis 8.10.3；中间件地址统一指向 192.168.20.88。
- Spring Boot 4.1.1 → 4.1.3；Spring Cloud 2025.1.3 → 2025.1.4；mybatis-plus 3.5.18.1、springdoc 3.2.1、lombok 1.18.48。
- 服务名与 Apollo 应用名统一 `xfjr-*` 前缀；模块目录去掉 service 后缀；Apollo 包路径对齐 `com.xfjr`。
