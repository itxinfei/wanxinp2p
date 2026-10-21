---
name: 依赖升级状态与限制
description: 2026-10全量依赖升级后的版本基线、回退项原因、后续优化方向
type: project
---

2026-10-21 完成全量依赖升级（commit ee46957），当前版本基线：
- Spring Boot 4.1.1 / Spring Cloud 2025.1.3 / JDK 25
- MyBatis-Plus 3.5.17（包名已迁移至 spring.service）
- Vite 8.3.5（Rolldown）/ vue-router 5.4.0 / Element Plus 2.9.x

**Why:** 用户要求将所有可升级技术栈升至最新，接受较高风险。

**How to apply:**
- OkHttp 保持 4.12.0：5.x 仅发布 Gradle Module Metadata 变体，Maven jar 为空壳（754字节），Maven 项目无法消费，除非迁移到 Gradle
- Spring Authorization Server 保持 1.5.0：7.x 完全移除 config.annotation.web.configurers 包（OAuth2AuthorizationServerConfigurer 不存在），升级需完全重写 SecurityConfig 为手动注册端点过滤器模式
- MyBatis-Plus 3.5.17：IService/ServiceImpl 已从 extension.service 迁移至 spring.service，类名不变
- Vite 8 manualChunks 仅支持函数形式（Rolldown 限制）
- IntegrationUserDetailsAuthenticationProvider 已改为 implements AuthenticationProvider（Spring Security 7.x 移除了 AbstractUserDetailsAuthenticationProvider）
- 后续优化项：Auth Server 7.x 迁移、Element Plus 按需导入