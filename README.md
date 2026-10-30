# 物业工单通

面向社区物业的**报修与派单系统**。业主扫码报修，客服派单，维修工接单处理，全程状态可追溯；AI 只做语义分类，派单决策由确定性规则完成。

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.3 · JDK 21 · MyBatis-Plus 3.5.7 · MySQL 8 · Spring Security + JWT |
| 管理后台 | Vue 3 + Vite + Element Plus + Pinia（路径 `/admin`） |
| 移动 H5 | Vue 3 + Vite + Vant 4 + Pinia（路径 `/m`） |
| 智能辅助 | DeepSeek API（仅分类与优先级，不参与派单决策） |
| 消息 | 站内通知 + SSE 实时推送 + 短信兜底 |

## 目录结构

```
物业工单通/
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/com/community/repair/
│   │   ├── controller/       # 参数校验与调用 Service
│   │   ├── service/          # 业务逻辑（状态机、派单）
│   │   ├── mapper/           # MyBatis-Plus Mapper
│   │   ├── entity/  dto/     # 实体与传输对象
│   │   ├── security/         # JWT 过滤器与 UserDetails
│   │   ├── config/           # 安全、MyBatis-Plus 配置
│   │   └── common/           # 统一响应、异常、错误码
│   └── src/main/resources/application.yml
├── admin-web/                # 管理后台（P4，尚未创建）
├── mobile-web/               # 移动 H5：报修、我的工单、维修端处理
│   ├── src/api/              # 请求封装
│   ├── src/views/            # 登录 / 报修 / 列表 / 详情
│   ├── src/stores/           # Pinia
│   └── src/utils/            # 图片压缩、状态字典
├── docs/
│   ├── 开发指导.md            # 完整开发方案
│   └── database/schema.sql   # 建表脚本
├── scripts/                  # 本地开发脚本
└── .env.example              # 环境变量模板
```

## 环境要求

- JDK 21、Maven 3.9+、Node 20+（pnpm）
- MySQL 8.0+

> 本机若未全局安装 JDK/Maven，可使用项目内 `.tools/` 目录（不纳入版本库）：
> 见 `docs/开发指导.md` 的"本地工具链"一节。

## 快速开始

### 1. 建库建表

```bash
mysql -uroot -p < docs/database/schema.sql
```

### 2. 配置环境变量

```powershell
Copy-Item .env.example .env
# 编辑 .env，至少填写 DB_PASSWORD
```

### 3. 启动后端

```powershell
.\scripts\dev-backend.ps1
```

访问 <http://localhost:8080/api/health>，返回 `{"code":0,"message":"ok","data":{"status":"ok","db":"up"}}` 即表示服务与数据库均正常。

### 4. 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 5. 启动移动端

```powershell
cd mobile-web
npm install
npm run dev
```

打开 <http://localhost:5174/m/>，用 `owner1 / 123456` 登录。

扫码报修的入口形如 <http://localhost:5174/m/report?point=B3-1201>，`point` 会自动拆成楼栋与房号。

## 实施进度

| 阶段 | 内容 | 状态 |
|---|---|---|
| P0 | 仓库骨架、建表脚本、开发文档 | ✅ |
| P1 | 后端骨架、健康检查、JWT 登录与角色鉴权 | ✅ |
| P2 | 工单核心：schema v2、六态状态机、流转日志、派单/接单/完工/确认 | ✅ |
| P3 | 移动 H5：扫码报修、图片压缩上传、我的工单、维修端接单/完工/确认/返修 | ✅ |
| P4 | 管理后台：工单中心（列表、筛选、详情、时间线、派单） | ⬜ |
| P5 | 评价与回访 | ⬜ |
| P6 | 消息推送：站内通知 + SSE + 短信兜底 | ⬜ |
| P7 | 自动派单规则 + 工作台看板 | ⬜ |
| P8 | 统计看板（报修量、超时率、响应/完工时长、评价分布） | ⬜ |
| P9 | 收费（简版）+ 基础配置（楼栋二维码、值班表、消息模板） | ⬜ |
| P10 | 部署（Nginx + HTTPS） | ⬜ |

## 设计要点

- **流程状态与费用状态解耦**：`tickets.status` 管流程，`tickets.fee_status` 管费用，避免维修中追加费用时状态机卡死。
- **状态机校验在 Service 层**：非法流转直接抛业务异常，不落到数据库再回滚。
- **AI 不决策**：模型输出 `{category, priority, skill}`，派单由规则完成（技能匹配 + 负载最低），解析失败自动回退人工派单。
- **SSE 鉴权用查询参数**：`EventSource` 无法设置请求头，故 `/sse/**` 接受 `?token=`，后端校验 JWT 后取用户，杜绝用 `uid` 参数伪造。

## 文档

- [开发指导](docs/开发指导.md)
- [数据库结构](docs/database/schema.sql)
