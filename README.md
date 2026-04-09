# Lottery System

简洁说明：基于 Spring Boot + Vue 3 的抽奖系统（后端 API + 前端 SPA）。

## 要求
- JDK 17+
- Maven wrapper 已包含（使用 `./mvnw` 或 `mvnw.cmd`）

## 快速开始

### 前端开发
前端位于 `lottery-frontend/` 目录：

```bash
cd lottery-frontend
npm install        # 安装依赖
npm run dev        # 开发服务器 (localhost:3000)
npm run build      # 生产构建 → src/main/resources/static/
```

### 后端开发
- 构建（Windows）：

- 运行 jar（示例使用 `dev` profile）：

```bash
java -jar target/lottery-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

- 开发运行（热启动）：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 配置
- 默认 profile：`prod`（见 [pom.xml](pom.xml)）
- 配置文件： [src/main/resources/application-dev.yml](src/main/resources/application-dev.yml) 、 [src/main/resources/application-prod.yml](src/main/resources/application-prod.yml)
- 主要外部依赖：MySQL、Redis、RabbitMQ；请在对应的 yml 中配置连接信息
- 图片输出目录：可通过环境变量 `PICTURE_DEST_PATH` 覆盖，默认 `${user.dir}/picture/`

## 技术栈
- 语言/平台：Java 17+
- 核心框架：Spring Boot 4
- 持久层：MyBatis-Plus + MySQL
- 缓存/消息：Redis、RabbitMQ
- 认证：JWT
- 常用库：Lombok、Hutool
- 日志：Logback
- 构建/运行：Maven / Maven Wrapper
- 前端：Vue 3 + Vite + Element Plus + Pinia

## 代码结构
- 后端入口： [src/main/java/com/julien/lotterysystem/LotterySystemApplication.java](src/main/java/com/julien/lotterysystem/LotterySystemApplication.java)
- 静态资源（前端构建输出）： [src/main/resources/static/](src/main/resources/static/)
- 前端源码： [lottery-frontend/](lottery-frontend/)
- 主要包： `controller` / `service` / `mapper` / `entity`

## 其他说明
- 构建时启用了资源过滤（pom.xml），会注入 profile 名称
- 关键版本：Spring Boot 4，Java 17（见 [pom.xml](pom.xml)）




