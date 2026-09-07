# 腾讯云服务器部署指南（Docker Compose）

本项目采用 Docker Compose 编排 **MySQL + 后端(Spring Boot) + 前端(Nginx)**，一次配置后，以后更新只需 `git pull` + 一条命令，无需重配环境。

## 一、服务器准备（一次性）

以 Ubuntu 22.04 / Debian 系为例：

```bash
# 1. 安装 Docker
curl -fsSL https://get.docker.com | sh

# 2. 安装 docker compose 插件
sudo apt-get update
sudo apt-get install -y docker-compose-plugin

# 3. 启动并设置开机自启
sudo systemctl enable --now docker

# 4. 验证
docker --version
docker compose version
```

> CentOS 系：用 `yum` 安装同理，核心是装好 `docker` 与 `docker compose` 插件。

## 二、安全组放行端口

在腾讯云控制台「云服务器 → 安全组」放行：

- **80**（HTTP，前端访问）
- **22**（SSH，管理）

> 若服务器开启了系统防火墙（ufw/firewalld），也要放行 80：
> ```bash
> sudo ufw allow 80/tcp
> ```

## 三、拉取代码并配置

```bash
# 1. 克隆仓库
git clone https://github.com/<你的用户名>/sql-learn.git
cd sql-learn

# 2. 生成环境变量文件并修改
cp .env.example .env
vim .env    # 修改 MySQL 密码与 APP_JWT_SECRET
```

`.env` 中需设置：

| 变量 | 说明 |
| ---- | ---- |
| `MYSQL_PASSWORD` | 应用连接 MySQL 的密码 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `APP_JWT_SECRET` | JWT 签名密钥（至少 32 字符随机串） |

## 四、构建并启动

```bash
docker compose up -d --build
```

首次构建需下载基础镜像与依赖，约数分钟。启动顺序：MySQL 就绪 → 后端 → 前端。

```bash
# 查看状态（三个服务应为 running/healthy）
docker compose ps

# 查看后端日志
docker compose logs -f backend
```

## 五、验证

浏览器访问 `http://<服务器公网IP>/`，确认可注册、登录、查看学习进度。

命令行快速检查：

```bash
curl http://localhost/
curl http://localhost/api/courses
```

## 六、日常更新流程

以后代码更新后，在服务器项目目录执行：

```bash
git pull
docker compose up -d --build
```

- 数据保存在 `mysql-data` 数据卷中，重建镜像/重启容器**不会丢失**用户与进度数据。
- 若只想重启（不改代码）：`docker compose restart`。

## 七、常用排障

```bash
docker compose ps                  # 查看服务状态
docker compose logs -f mysql       # 查看 MySQL 日志
docker compose logs -f backend     # 查看后端日志
docker compose logs -f frontend    # 查看 nginx 日志
docker compose down                # 停止并删除容器（数据卷保留）
docker compose down -v             # 连同数据卷一起删除（会清空数据，慎用）
```

## 八、本地开发（不使用 Docker）

```bash
# 后端（默认 H2，本地无需 MySQL）
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm run dev
```
