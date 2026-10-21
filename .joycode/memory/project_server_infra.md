---
name: 服务器中间件部署状态
description: 192.168.20.130服务器Docker中间件拓扑、端口、网络与验证方式（2026-10-21全量通过）
type: project
---

服务器192.168.20.130(xfwl,sudo)中间件全量部署验证通过(2026-10-21)。

容器：mysql8(3306,32库) | rmqnamesrv(9876)+rmqbroker(10911,RocketMQ5.3.1) | apollo三件套(8080/8092/8070) | elasticsearch(9200,9.4.5,green)

关键：Docker网络xfjr-net内用容器名互联；Apollo2.4.0官方SQL已导入(configdb19表+portaldb18表)，本地备份.joycode/apollo*-v2.4.0.sql；RocketMQ5.x的mqadmin全路径/home/rocketmq/rocketmq-5.3.1/bin/mqadmin；broker.conf须无BOM+LF换行否则Properties.load失败。

**Why:** 后续微服务部署排障依赖此拓扑
**How to apply:** 用.joycode/ssh_exec.sh操作服务器，verify_all.sh验证；注意ES9.4.5与项目预期7.x/8.x有兼容风险