![](docs/log2.png)
<p align="center">
  <a href="https://gitee.com/itxinfei">
    <img alt="code style" src="https://img.shields.io/badge/心飞为你飞-https%3A%2F%2Fgitee.com%2Fitxinfei-green">
  </a> 
  <a href="https://qm.qq.com/cgi-bin/qm/qr?k=9yLlyD1dRBL97xmBKw43zRt0-6xg8ohb&jump_from=webapi">
    <img alt="code style" src="https://img.shields.io/badge/QQ群-863662849-red">
  </a> 
  <a href="http://mail.qq.com/cgi-bin/qm_share?t=qm_mailme&email=f0hLSE9OTkdHTT8ODlEcEBI">
    <img alt="code style" src="https://img.shields.io/badge/mail-747011882@qq.com-red">
  </a> 
  <a href=" ">
    <img alt="code style" src="https://img.shields.io/badge/JDK-1.8%2B-brightgreen">
  </a> 
  <a href=" ">
    <img alt="maven" src="https://img.shields.io/badge/maven-3.6.3%2B-yellowgreen">
  </a>
  <a href=" ">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache-green">
  </a> 
</p>

- 前端工程
https://gitee.com/itxinfei/wanxinp2p-portal

- 教学视频
https://www.bilibili.com/video/BV1Ub4y1f7rk?share_source=copy_web

- 在线体验
http://wanxinp2p-admin-java.itheima.net/

#### 一、项目背景
P2P金融又叫P2P信贷，P2P是 peer-to-peer 或 person-to-person 的简写，意思是个人对个人，P2P金融是指个人与个人间的小额借贷交易，一般需要借助电子商务专业网络平台帮助借贷双方确立借贷关系并完成相关交易手续。

![输入图片说明](https://images.gitee.com/uploads/images/2020/0815/130435_9db706b6_800553.png "屏幕截图.png")

#### 二、项目概述 
本次课程开发的万信金融是一款面向互联网大众提供的理财服务和个人消费信贷服务的金融平台，依托大数据风控技术，为用户提供方便、快捷、安心的P2P金融服务。本项目包括交易平台和业务支撑两个部分，交易平台主要实现理财服务，包括：借钱、出借等模块，业务支撑包括：标的管理、对账管理、风控管理等模块。项目采用先进的互联网技术进行研发，保证了P2P双方交易的安全性、快捷性及稳定性。

#### 三、功能模块
![输入图片说明](https://images.gitee.com/uploads/images/2020/0815/130453_5d60c2c7_800553.png "屏幕截图.png")

#### 四、技术架构图
![](docs/技术架构图.png)

#### 五、技术解决方案
|  主要的解决方案    |
| ---- |
|  1、微服务技术应用于P2P金融业务解决方案   |
| 2、接口规范SpringBoot+Swagger|
| 3、持久层编码 MyBatis Plus|
| 4、分布式系统配置中心：Apollo|
| 5、UAA认证方案：Spring Security Oauth2+JWT+ZUUL|
| 6、分布式事务解决方案（RocketMQ、Hmily、requestNo同步机制）|
| 7、分库分表解决方案：Sharding-jdbc|
| 8 、分布式任务调度方案：Elastic-job|
| 9、安全交易方案：HTTPS+SHA1withRSA|
| 10、身份认证方案：百度AI|
| 11、短信验证系统方案：短信验证服务+第三方短信平台（腾讯）|

#### 六、P2P平台服务
| 服务端口                                                 |
| -------------------------------------------------------- |
| 注册与发现 (wanxinp2p-discover-server)：53000            |
| 网关 wanxinp2p-gateway-server)：53010                    |
| uaa(wanxinp2p-uaa-service) ：53020                       |
| 统一账号服务 (wanxinp2p-account-service)：53030          |
| 用户中心服务 (wanxinp2p-consumer-service)：53050         |
| 交易中心服务 (wanxinp2p-transaction-service)：53060      |
| 存管代理服务 (wanxinp2p-depository-agent-service)：53070 |
| 还款服务 (wanxinp2p-repayment-service)：53080            |
| 文件服务 (wanxinp2p-file-service)：56082                 |
| 短信验证码服务 (sailing)：56085                          |
| P2P 平台前端(wanxinp2p_front)：8081                      |
| P2P 平台管理端后台(wanxinp2p-manager)：8079                                 |
| 银行存管系统：55010                                      |

| 数据库清单                                |
| ----------------------------------------- |
| p2p_uaa 统一认证数据                      |
| p2p_account 统一账户数据                  |
| p2p_consumer 用户中心数据                 |
| p2p_transaction_0 交易中心数据库1         |
| p2p_transaction_1 交易中心数据库2         |
| p2p_undo_log 分布式事务框架Hmily数据库    |
| p2p_repayment 还款中心数据                |
| p2p_file 文件存储服务                     |
| p2p_bank_depository 银行存管系统          |
| p2p_depository_agent 银行存管代理服务数据 |
| p2p_reconciliation 对账数据               |

#### 七、项目部署
##### 1、导入数据库
- 安装MySQL数据库(v5.6.5+)
- 执行wanxinp2p-init.sql 创建P2P平台数据库并导入初始数据
- 执行wanxindepository-init.sql 创建银行存管系统数据库并导入初始数据

##### 2、搭建中间件
Apollo安装和导入sql,添加配置文件。

##### 3、编译工程
添加vm参数，在application.yml文件头部。比如：-Denv=dev -Dapollo.cluster=DEFAULT -Dserver.port=53010


**注：以后有时间在整理文档，安装部署的注意事项吧。项目基本功能都写完了，有问题有需求可以在群（QQ群：863662849 ）里咨询，感谢！！！**