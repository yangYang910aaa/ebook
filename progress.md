# 进度日志

## 会话：2026-09-01

### 阶段 1：需求确认与差异分析（in_progress）
- **状态：** in_progress
- **开始时间：** 2026-09-01
- 执行的操作：
  - 通读需求文档全文（E:\Downloads\数字电子书-项目需求分析文档.md）
  - 盘点 springboot 后端已有代码（pom、application.yml、controller/common/config/security/websocket/service/mapper）
  - 确认 vue 目录为空、wiki.sql 不存在
  - 创建三个规划文件
  - 与用户确认技术选型：保留 Spring Boot 4.1.1 + MyBatis + HikariCP + Jackson（不加 Druid/fastjson）
  - 用户确认项目目标：完整跑通、可验收可演示
  - 确认 git 现状：D:\work 尚无仓库，计划根目录初始化单一仓库（含 springboot + vue）
  - 在 Ubuntu 虚拟机安装 Redis 7.0.15（apt + systemd 自启），配置 bind 0.0.0.0 与 requirepass 123456
  - 从 Windows 侧验证 Redis：AUTH +OK、PING PONG、SET/GET 正常（期间排查过 bind 写错为 127.0.0.0 的问题）
  - 确认 MySQL 账号 root/123456 可用（MySQL 9.3.0，位于 D:\MySQL\MySQL Server 9.3）
  - 编写建表脚本 D:\work\springboot\sql\wiki.sql 并执行：wiki 库 + 6 张表创建成功，字段与文档 6.2 核对一致
- 创建/修改的文件：
  - D:\work\task_plan.md
  - D:\work\findings.md
  - D:\work\progress.md

## 测试结果
| 测试 | 输入 | 预期结果 | 实际结果 | 状态 |
|------|------|---------|---------|------|
| （暂无，未到联调阶段） | | | | |

## 错误日志
| 时间戳 | 错误 | 尝试次数 | 解决方案 |
|--------|------|---------|---------|
| （暂无） | | 1 | |

## 五问重启检查
| 问题 | 答案 |
|------|------|
| 我在哪里？ | 阶段 1：需求确认与差异分析 |
| 我要去哪里？ | 待用户确认技术决策后进入阶段 2 数据与环境准备 |
| 目标是什么？ | 按需求文档完成前后端开发并过验收 |
| 我学到了什么？ | 见 findings.md（现状盘点与差异） |
| 我做了什么？ | 通读需求 + 摸底现有工程 + 创建规划文件 |

---
*每个阶段完成后或遇到错误时更新此文件*
