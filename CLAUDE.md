# CLAUDE.md

## 项目概述

抽奖系统 (Lottery-system)
- 后端：Spring Boot + MyBatis Plus + Redis + RabbitMQ
- 前端：Vue 3 + Vite + Element Plus (位于 `lottery-frontend/`)

## Checkpoint - 当前进度

### 已完成
- [x] 阶段一：项目初始化（当前会话直接执行）
  - Vue 3 + Vite + Element Plus 项目创建
  - 路由和 Pinia 状态管理配置
  - API 请求封装 (Axios 拦截器)
  - SCSS 样式系统 (新闻纸复古风格)
  - 9个页面视图全部创建
  - 通用组件和布局组件
  - 构建产物输出到 `src/main/resources/static/`
- [x] Bug 修复
  - API 响应格式处理 (Result<T> wrapper)
  - JWT token 解码获取用户身份
  - 路由守卫权限检查
  - 中奖记录查询接口

### 待完成
- [ ] 阶段二：模块顺序开发（派发 Agent 执行）
  - [ ] Agent 1: 组件库优化
  - [ ] Agent 2: 页面框架优化
  - [ ] Agent 3: 动画系统
  - [ ] Agent 4: 认证页面优化
  - [ ] Agent 5: 业务页面优化
  - [ ] Agent 6: 用户相关页面优化
- [ ] 阶段三：集成部署

### 关键技术决策
- 用户身份从 JWT token 解码获取，不调用 API
- 后端 `Result<T>` 响应格式：`{ code, message, data }`
- 登录仅限管理员账号 (`/user/admin/passwordLogin`)

## 常用命令

### 后端
```bash
mvn spring-boot:run          # 启动后端服务
java -jar target/lottery-system-0.0.1-SNAPSHOT.jar  # jar 包运行
```

### 前端
```bash
cd lottery-frontend
npm install                  # 安装依赖
npm run dev                  # 开发模式 (localhost:3000)
npm run build                # 生产构建 → ../src/main/resources/static/
```

## 后端 API 端点

### 用户模块 `/user`
| 端点 | 方法 | 描述 |
|------|------|------|
| `/register` | POST | 用户注册 |
| `/sendEmailCode` | POST | 发送邮箱验证码 |
| `/emailRegister` | POST | 邮箱验证码注册 |
| `/admin/sendEmailCode` | POST | 发送管理员邮箱验证码 |
| `/admin/emailLogin` | POST | 管理员邮箱登录 |
| `/admin/passwordLogin` | POST | 管理员密码登录 |
| `/getListInfo` | GET | 获取用户列表 |

### 活动模块 `/activity`
| 端点 | 方法 | 描述 |
|------|------|------|
| `/create` | POST | 创建活动 |
| `/queryList` | GET | 分页查询活动列表 |
| `/getDetail` | GET | 获取活动详情 |

### 抽奖模块
| 端点 | 方法 | 描述 |
|------|------|------|
| `/drawPrize` | POST | 抽奖 |
| `/getWinningRecords` | POST | 获取中奖记录 |

### 奖品模块 `/prize`
| 端点 | 方法 | 描述 |
|------|------|------|
| `/getList` | GET | 分页获取奖品列表 |
| `/create` | POST | 创建奖品 (multipart) |

## 相关文档

- 设计规范：`docs/superpowers/specs/2026-04-06-newspaper-retro-frontend-redesign.md`
- 实施计划：`docs/superpowers/plans/2026-04-06-newspaper-retro-frontend.md`
- Agent 并发限制：顺序执行（每次只派一个 Agent）
