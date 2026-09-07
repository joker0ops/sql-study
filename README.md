# 关系型数据库学习网站（SQL Learn）

一个基于 **Spring Boot + Vue 3** 的关系型数据库学习网站，提供用户注册登录、教程浏览与个人学习进度管理功能。

教程资料来源于 [菜鸟教程 - 数据库板块](https://www.runoob.com/)，涵盖三大课程：

- **SQL 教程**（结构化查询语言）
- **MySQL 教程**
- **PostgreSQL 教程**

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 后端 | Java 17、Spring Boot 3.2、Spring Data JPA、H2（本地）/ MySQL（生产）、JWT 认证 |
| 前端 | Vue 3、Vite、Vue Router、Pinia、Axios、Element Plus |

## 项目结构

```
sql-study/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/sqlearn/
│       │   ├── config/         # Web 配置、数据初始化
│       │   ├── controller/     # 认证、用户、课程、进度接口
│       │   ├── dto/            # 请求/响应对象
│       │   ├── entity/         # JPA 实体（用户、课程、章节、进度）
│       │   ├── exception/      # 全局异常处理
│       │   ├── repository/     # 数据访问层
│       │   ├── security/       # JWT 工具与认证拦截器
│       │   └── service/        # 业务逻辑
│       └── resources/
│           ├── application.yml # 应用配置
│           └── data/courses.json # 教程种子数据（菜鸟教程目录）
├── frontend/                   # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js          # 开发代理 /api -> localhost:8080
│   └── src/
│       ├── api/                # Axios 封装与接口
│       ├── router/             # 路由与登录守卫
│       ├── stores/             # Pinia 状态（认证）
│       └── views/              # 登录/注册/首页/课程/进度页面
├── docker-compose.yml          # 生产编排（MySQL + 后端 + 前端）
├── .env.example                # 环境变量模板（复制为 .env 使用）
├── DEPLOY.md                   # 腾讯云 Docker 部署指南
└── maven-settings.xml          # 项目级 Maven 配置（本地仓库与镜像）
```

> 说明：`backend/.mvn/maven.config` 会自动加载根目录的 `maven-settings.xml`，把依赖缓存到用户目录的 `.m2` 仓库并使用阿里云镜像。这样即使本机全局 Maven 的本地仓库不可写、镜像失效，也能直接 `mvn` 构建。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+（推荐 20+）

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`，使用内嵌 H2 文件数据库（数据保存在 `backend/data/` 目录，重启不丢失）。

> 首次启动会自动写入教程种子数据（课程与章节目录，来源菜鸟教程）。

### 2. 启动前端

另开一个终端：

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，并通过 Vite 代理将 `/api` 请求转发到后端 `8080` 端口。浏览器访问 `http://localhost:5173` 即可使用。

### 3. 打包部署

```bash
# 后端
cd backend && mvn -DskipTests package   # 生成 backend/target/sql-learn-backend-0.0.1-SNAPSHOT.jar

# 前端
cd frontend && npm run build            # 生成 frontend/dist/ 静态资源
```

## Docker 部署（生产）

生产环境使用 Docker Compose 一键编排 MySQL + 后端 + 前端（Nginx），数据持久化到数据卷，更新无需重配环境：

```bash
cp .env.example .env   # 修改 MySQL 密码与 JWT 密钥
docker compose up -d --build
```

详细步骤（含腾讯云服务器准备、安全组、更新流程）见 [DEPLOY.md](./DEPLOY.md)。

## 功能说明

1. **注册 / 登录**：用户名 + 邮箱 + 密码注册，密码使用 BCrypt 加密存储；登录后颁发 JWT，前端本地保存。
2. **课程浏览**：三大课程（SQL / MySQL / PostgreSQL），每个课程包含若干章节，章节标题与目录来源于菜鸟教程，点击「查看教程」跳转到菜鸟教程原文。
3. **学习进度**：登录用户可对每个章节标记「已完成」；首页与「学习进度」页展示每门课程及总体完成百分比。

## 主要接口

| 方法 | 路径 | 说明 | 认证 |
| ---- | ---- | ---- | ---- |
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/user/me` | 当前用户信息 | 是 |
| GET | `/api/courses` | 课程列表 | 否 |
| GET | `/api/courses/{id}` | 课程详情（含章节） | 否 |
| GET | `/api/progress/summary` | 学习进度概览 | 是 |
| GET | `/api/progress/course/{courseId}` | 某课程已完成章节 | 是 |
| POST | `/api/progress/{lessonId}/toggle` | 切换章节完成状态 | 是 |

> 认证方式：请求头 `Authorization: Bearer <token>`。

## 常见问题

**Q：`mvn` 构建报“本地仓库无法写入 / 无法下载依赖”？**

本机全局 Maven 的本地仓库指向 `D:\ProgramFiles\Maven\maven-repository`（只读），且其镜像地址已失效。项目已通过 `backend/.mvn/maven.config` 自动加载根目录的 `maven-settings.xml`，把依赖缓存到工作区 `.m2` 目录并改用阿里云镜像，因此直接执行 `mvn` 即可，无需额外参数。

**Q：`npm install` 报 EPERM（无法写入 node_cache）？**

本机 npm 全局缓存被配置到 `D:\ProgramFiles\NodeJs\node_cache`（只读）。可指定可写缓存目录重新安装：

```bash
cd frontend
npm install --cache E:\Project\sql-study\.npm-cache
```

## 说明

- 教程内容版权归 [菜鸟教程](https://www.runoob.com/) 所有，本项目仅以目录/链接形式引用，未复制其正文内容。
- 生产部署已通过 Docker Compose 使用 MySQL 存储，JWT 密钥由 `.env` 提供（`APP_JWT_SECRET`），务必替换默认值。
