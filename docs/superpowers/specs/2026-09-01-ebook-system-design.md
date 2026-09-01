# 数字电子书系统 设计方案

- 日期：2026-09-01
- 状态：已与用户确认，进入实施
- 依据：《数字电子书-项目需求分析文档》V1.0

## 1. 目标

构建可运行、可验收演示的电子书在线阅读平台（前后端分离），覆盖文档全部功能需求（FR-01~FR-17）与验收要点。

## 2. 技术选型（已确认）

- 后端：Spring Boot 4.1.1 / Java 21 / MyBatis / HikariCP / Jackson / springdoc / WebSocket
- 前端：Vue 3 + TypeScript + Ant Design Vue 2.x + Vuex + axios + wangEditor 4.6.3 + ECharts
- 数据库：MySQL（本机 9.3.0，库 wiki，root/123456），6 张表已建
- 缓存：Redis（Ubuntu 虚拟机 192.168.30.128:6379，密码 123456）
- 端口：后端 8088；前端开发服务器 8080/8081
- 认证：雪花 token 存 Redis（TTL 24h）+ 登录拦截器 + 白名单（替换现有 Spring Security + JWT）

## 3. 后端架构

### 3.1 分层

Controller → Service → Mapper；DTO（req/resp）与实体分离；统一响应 `Result{success, message, content}`；分页 content 为 `{total, list}`。

### 3.2 公共机制

- 异常：`BusinessException` + 错误码枚举；全局处理器分三类（参数校验 / 业务异常 / 未知异常）
- 认证：登录（login_name + MD5 密码）→ 雪花 token → Redis（TTL 24h）→ 拦截器白名单 + OPTIONS 放行 → 线程上下文 `UserContext`
- 日志：AOP 切面打印 Controller 请求（地址/方法/远程 IP/参数脱敏 password,file/结果/耗时），MDC 记录雪花 LOG_ID
- 工具：`CopyUtil`（Bean 拷贝）、`SnowflakeIdWorker`（ID 生成）、`SessionStorage`（前端对应）
- 事务：doc 与 content 同事务保存；删除文档递归收集子孙 id 批量级联删除
- 点赞：Redis 按 IP+doc 去重（游客可赞），成功后 `@Async` 推送 WebSocket 通知
- 定时任务：电子书统计聚合（批量更新 doc_count/view_count/vote_count）；每日快照（同书同日唯一，增量对比昨日）
- 上传：封面 UUID 重命名，限 jpg/jpeg/gif/png、≤10MB，存前端静态目录相对路径

### 3.3 模块与接口

按文档第 7 节接口清单实现：user（登录/退出/CRUD/重置密码）、category（树形查询/CRUD）、ebook（分页/模糊/CRUD/上传）、doc（树/内容/保存事务/级联删除/点赞）、ebook-snapshot（统计）。

## 4. 前端架构

- axios 封装（请求头自动带 token、统一错误提示）；路由守卫（`/admin/*` 需登录）；Vuex + sessionStorage 存用户（刷新不丢）；多环境 `.env.dev/.env.prod`
- 布局：顶栏（导航 + 登录状态）+ 左侧分类菜单 + 内容区 + 页脚（WebSocket 连接）
- 前台：首页（电子书网格 + 欢迎统计页）、分类浏览、阅读页（文档树 + 富文本 + 点赞）、登录弹窗、统计卡片 + 30 天趋势（ECharts）、通知
- 后台（/admin/*）：电子书管理（分页/模糊/级联分类/上传）、分类管理（树形表格）、文档管理（树 + wangEditor + 预览 + 级联删除）、用户管理

## 5. 实施顺序

1. 后端基础层：Result / BusinessException / 全局异常 / DTO / 工具类 / 配置对齐（阶段 3）
2. 认证与鉴权：登录/退出、Redis token、拦截器、密码校验（阶段 4）
3. 业务模块：分类/电子书/文档/用户/上传（阶段 5）
4. 统计与快照：定时任务 + 统计接口（阶段 6）
5. WebSocket 通知 + AOP 日志（阶段 7）
6. 前端框架：脚手架/路由/axios/Vuex/布局（阶段 8）
7. 前台页面（阶段 9）→ 后台页面（阶段 10）
8. 联调验收（阶段 11）→ 部署交付（阶段 12）

## 6. 风险与注意

- 沙箱禁止写 `.git`：git 提交/推送由用户在本机执行（VSCode 源代码管理或命令行）
- 沙箱默认禁对外 TCP：联调时临时授予网络权限，或用户在本机验证
- 现有工程与文档差异已按决策处理（保留 Spring Boot 4.1.1 + MyBatis，不加 Druid/fastjson）
