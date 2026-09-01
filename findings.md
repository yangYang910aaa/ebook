# 发现与决策

## 需求
- 系统：数字电子书（Wiki 电子书/文档网站），B/S 前后端分离
- 两级内容体系：电子书 → 文档（章节）；文档自身树形；content 与 doc 主键 1:1
- 角色：游客（浏览/阅读/点赞/统计/通知）+ 登录用户（后台管理 /admin/*）
- 功能：FR-01~FR-17（布局、首页、阅读、点赞、登录、路由权限、统计报表、通知、后台四类管理、SSO 鉴权、统一响应、日志、定时任务、WebSocket）
- 数据：6 张表 category / ebook / doc / content / user / ebook_snapshot
- 验收：文档第 8 节 7 条要点
- 技术选型约束（文档）：后端 Spring Boot 3.1.6 + MyBatis-Plus 3.5.3.1 + Druid + MySQL + Redis + AOP + WebSocket + fastjson + Lombok；前端 Vue3 + TS + Ant Design Vue 2.x + Vuex + axios + wangEditor 4.6.3 + ECharts；端口 8088；库 wiki；token 存 Redis 24h

## 研究发现（现状盘点）

### springboot 后端（D:\work\springboot）
- 栈：Spring Boot 4.1.1 / Java 21 / MyBatis（非 Plus）/ springdoc / jjwt / RocketMQ（已排除）/ Aliyun OSS（未配置）/ Redis（配置类有，连接未启用）
- 端口 8080，datasource 库名 ebook（文档要求 8088 / wiki）
- 已有可复用：
  - Result{code, message, data}（需改为 {success, message, content}）
  - GlobalExceptionHandler（已有 @Valid 与兜底，缺 BusinessException + 错误码）
  - RedisConfig、SwaggerConfig
  - WebSocketConfig + ChatWebSocketHandler（当前 /ws/chat 广播，需改 /ws/{token} + 异步通知）
- 需改造：
  - AuthController：演示登录不校验密码 → 真实登录
  - SecurityConfig：Spring Security + JWT → 文档要求登录拦截器 + Redis token
  - JwtUtil：JWT → 雪花 token 存 Redis（或保留，待决策）
  - User 实体：username/nickname → login_name/name；密码 char(32) MD5
  - UserServiceImpl：当前没有 @Service 注解（注释注明等接入数据库后启用）
- 主类 EBookApplication；测试 EBookApplicationTests（仅 contextLoads）

### vue 前端（D:\work\vue）
- 空目录，需从零搭建

### 其他
- E:\Downloads 与 D:\work 均未找到 wiki.sql（Downloads 下只有 department.sql / employee.sql）
- git：D:\work 尚无仓库；用户未建 GitHub 仓库（本地仓库先行，远端后续可选）

## 本地环境检查（2026-09-01）
- MySQL：已装可用，Windows 服务 MySQL93 正在运行；用户经 VSCode 连接成功，用 Navicat 查看
- Redis：未安装。无 redis 服务、6379 端口未监听、PATH 无 redis-server；D:\Maven\mvnrep\redis 为 Maven 客户端 jar，Navicat 的 redis++.dll 为连接组件，均非服务端
- 结论：阶段 2 需先安装 Redis
- 方案更新：用户有 VMware Workstation Pro 的 Ubuntu 虚拟机 → 优先在 Ubuntu 内 apt 安装 redis-server（systemd 自启），Windows 后端连虚拟机 IP:6379；需配置 bind 与 requirepass，并确认主机可达虚拟机 IP
- 连通性验证：Windows 主机 ping 192.168.30.128 4/4 通（0ms）；VMnet8(NAT) 网卡 192.168.30.1 ↔ 虚拟机 192.168.30.128 同网段；虚拟机内 22 端口未开（未装 SSH，正常，不影响）；虚拟机内装有 Docker（172.17/18/19/20/21.0.1 网段），后续可选
- Redis 部署完成（2026-09-01）：Ubuntu 虚拟机 apt 安装 redis-server 7.0.15，systemd 自启；监听 0.0.0.0:6379；requirepass=123456；Windows 侧验证通过（AUTH +OK / PING PONG / SET/GET 正常）
- MySQL：本机 9.3.0（D:\MySQL\MySQL Server 9.3），账号 root/123456 已验证可连；wiki 库 + 6 张表已建（脚本 D:\work\springboot\sql\wiki.sql），字段与文档 6.2 一致
- 环境注意：本会话执行环境（沙箱）默认禁止对外 TCP（连 8.8.8.8:80 也被拒）；联调时需临时授予网络权限，或由用户在本机 PowerShell/Navicat 验证
- 备注：application.yml 中 MySQL 账号 root/root 是否与本机一致待建库时验证

## 技术决策
| 决策 | 理由 |
|------|------|
| （倾向保留）Spring Boot 4.1.1，不降级到 3.1.6 | 版本高低不影响功能；第三方库有适配版本即可 |
| （倾向按需）MyBatis-Plus 仅在验收要求时引入 | 非 Plus 只是多手写 CRUD/分页 SQL，功能不缺失 |
| （倾向不加）Druid | 现有 HikariCP 更新更快，Druid 优势仅在监控/防火墙 |
| （倾向不加）fastjson | 现有 Jackson 能力相当且更稳；fastjson 1.x 有历史漏洞 |
| （待确认）认证方案按文档 Redis token + 拦截器 | 贴合教程与验收 |
| 统一响应按文档 {success, message, content} | 前端按文档对接 |

## 技术选型对比结论（2026-09-01）
- Spring Boot 版本：3.1.6 → 4.1.1 属升级，API 大体兼容；真实风险是第三方 starter 的适配版本（现有 pom 的 mybatis starter 4.1.0 已证明可用）与教程代码 API 差异，后者按实际版本适配即可。
- MyBatis vs MyBatis-Plus：Plus 是增强封装（BaseMapper 免写 CRUD、分页插件、LambdaQueryWrapper、逻辑删除等），非必须。非 Plus 的代价 = 每表手写增删改查 SQL + 自写分页（LIMIT+count）+ 手写动态条件 SQL。本项目仅 6 张表，手写完全可控；且复杂 SQL 即便用 Plus 也还是写 XML，两者可共存。
- HikariCP vs Druid：HikariCP 是 Spring Boot 默认且性能更好；Druid 多监控面板/SQL 防火墙/慢 SQL 日志。非必须。
- Jackson vs fastjson：Jackson 为 Spring Boot 默认，能力相当、维护更稳；fastjson 1.x（含 1.2.70）有历史安全漏洞。不建议引入。

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| wiki.sql 缺失 | 请用户提供；否则按文档 6.2 表清单建表 |
| 现有工程与文档技术栈不一致 | 待用户拍板（关键问题 1） |

## 资源
- 需求文档：E:\Downloads\数字电子书-项目需求分析文档.md
- 后端工程：D:\work\springboot
- 前端工程（空）：D:\work\vue

## 视觉/浏览器发现
- 无（未进行浏览器/图片操作）
