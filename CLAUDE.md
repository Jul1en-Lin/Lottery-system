# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

抽奖系统 (Lottery-system) - 基于 Spring Boot + Vue 3 的抽奖系统（后端 API + 前端 SPA）

- **后端**：Spring Boot 4 + MyBatis Plus + Redis + RabbitMQ + JWT
- **前端**：Vue 3 + Vite + Element Plus + Pinia（位于 `lottery-frontend/`）
- **设计主题**：新闻纸复古风格（泛黄纸张、手写字体、邮戳印章）

## 常用命令

### 后端（Maven）
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev    # 开发运行（热启动）
mvn clean package                                      # 构建 jar（跳过测试）
java -jar target/lottery-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev  # 运行 jar
```

### 前端
```bash
cd lottery-frontend
npm install                  # 安装依赖
npm run dev                  # 开发服务器 (localhost:3000)
npm run build                # 生产构建 → ../src/main/resources/static/
```

## 架构概览

### 后端结构
```
src/main/java/com/julien/lotterysystem/
├── common/                  # 公共组件
│   ├── advice/             # 全局异常处理和响应包装
│   ├── config/             # 配置类（Redis、RabbitMQ、MyBatis、Web）
│   ├── constants/          # 常量定义
│   ├── enums/              # 枚举类
│   ├── exception/          # 自定义异常
│   ├── interceptor/        # 登录拦截器
│   └── utils/              # 工具类（JWT、AES、MD5）
├── controller/             # 控制器层
├── service/                # 服务层
├── mapper/                 # MyBatis-Plus 数据访问层
└── entity/                 # 实体类
```

### 前端结构
```
lottery-frontend/src/
├── api/
│   ├── index.js            # Axios 实例（拦截器处理 Result<T>）
│   └── modules/            # API 模块（user.js, activity.js, prize.js）
├── components/
│   ├── common/             # 通用组件（PaperCard、Stamp、InkButton）
│   ├── layout/             # 布局组件（NavBar、Footer）
│   └── business/           # 业务组件（LotteryCard、PrizeList、ScratchArea）
├── views/                  # 页面视图（9个页面）
├── stores/                 # Pinia 状态管理（user.js, activity.js）
├── styles/                 # SCSS 样式系统
├── utils/
│   └── index.js            # JWT 解码工具
└── router/
    └── index.js            # 路由配置（含权限守卫）
```

## 关键技术决策

### 后端
- **响应格式**：统一 `Result<T>` 包装器 `{ code, message, data }`
- **认证方式**：JWT token，从 HTTP Header `Authorization: Bearer {token}` 获取
- **管理员接口**：登录仅限 `/user/admin/passwordLogin` 和 `/user/admin/emailLogin`
- **文件存储**：图片上传后存储在 `PICTURE_DEST_PATH` 目录（默认 `${user.dir}/picture/`）

### 前端
- **用户身份获取**：从 JWT token 解码获取（不调用 API），使用 `decodeJwt()` 工具函数
- **API 响应处理**：Axios 拦截器自动处理 `Result<T>` 格式，业务代码直接拿到 `result.data`
- **构建输出**：`vite.config.js` 配置 `outDir: '../src/main/resources/static'`
- **样式系统**：新闻纸复古风格，配色定义在 `styles/variables.scss`

## 配置文件

- **默认 Profile**：`prod`（见 pom.xml）
- **开发配置**：`src/main/resources/application-dev.yml`
- **生产配置**：`src/main/resources/application-prod.yml`
- **外部依赖**：MySQL、Redis、RabbitMQ（在 yml 中配置连接信息）

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
| `/create` | POST | 创建奖品 (multipart/form-data) |

## 进度记录 (Progress Records)

### 2026-04-11
- **类型**: Bugfix
- **范围**: 前端奖品图片显示 (`PrizeList.vue`)
- **变更摘要**:
  - 修复活动详情页奖品图片无法显示的问题
  - 后端返回的只有文件名，前端拼接为 `/picture/${filename}`
  - 添加 `getFullImageUrl()` 方法处理图片URL
- **里程碑**: 修复 issue #3，奖品图片现在正确显示

### 2026-04-11
- **类型**: Bugfix
- **范围**: 前端活动状态显示 (`LotteryCard.vue`)
- **变更摘要**:
  - 修复活动列表页全部显示"已结束"的问题
  - 原因：不同接口返回不同字段，列表接口返回 `valid`，详情接口返回 `status`
  - `LotteryCard` 用于活动列表，应使用 `activity.valid`
  - `ActivityDetailView` 用于活动详情，应使用 `activity.status`
- **里程碑**: 修复活动列表页状态显示问题

### 2026-04-11
- **类型**: Bugfix
- **范围**: 前端首页 (`lottery-frontend/src/views/HomeView.vue`)
- **变更摘要**:
  - 修复"查看全部活动"按钮点击无反应的问题
  - 将按钮点击事件从 `scrollToActivities` 改为 `goToActivities`
  - 删除不再使用的 `scrollToActivities` 函数
- **里程碑**: 修复 issue #1，按钮现在正确跳转到活动列表页面

## 当前进度

### 已完成
- [x] 阶段一：项目初始化（Vue 3 + Vite + Element Plus）
- [x] Bug 修复（API 响应格式、JWT token 解码、路由守卫）
- [x] 阶段二：模块顺序开发（组件库、页面框架、动画系统、认证页面、业务页面、用户页面）

### 待完成
- [ ] 阶段三：集成部署测试

## 相关文档

- 设计规范：`docs/superpowers/specs/2026-04-06-newspaper-retro-frontend-redesign.md`
- 实施计划：`docs/superpowers/plans/2026-04-06-newspaper-retro-frontend.md`

## 开发核心准则 (Development Core Guidelines)

1. **文档更新义务**：在项目完成里程碑或主要新增内容后，更新 `docs/project_spec.md` 文件，确保规格说明与实现保持同步。

2. **提交规范**：在 git 进行提交时使用 `/update-docs-and-commit` 命令，自动将变更摘要记录到文档并生成规范的 commit message。

3. **规格溯源原则**：所有的逻辑实现必须溯源至 `docs/project_spec.md`。若代码实现与规格说明不符，以规格说明为准；若需要修改规格，必须先询问用户是否要更新文档，获得确认后再修改代码。

## Project Rules

### 文档更新规则

1. **架构对齐**：每当引入新的第三方库、修改核心类交互或调整数据库模式时，必须同步更新 `docs/architecture.md`。

2. **变更溯源**：每次 Git 提交前，需在 `docs/changelog.md` 中追加一条简要变更记录。

3. **进度锚定**：在每个任务（Issue）开始前，先读取 `docs/project_status.md` 确认当前里程碑；任务完成后，更新该文件的"已完成工作"与"待办与后续计划"部分。
