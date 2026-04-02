# 🚀 ChainPass 快速启动指南

## 一、前置条件

确保你已安装以下工具：

| 工具 | 版本 | 检查命令 |
|------|------|----------|
| Node.js | >= 20.19.0 | `node -v` |
| pnpm | >= 9.0.0 | `pnpm -v` |
| Java | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| Docker | 20.10+ | `docker -v` |

---

## 二、一键启动（推荐）

### Windows 用户

```powershell
# 1. 进入项目目录
cd chainpass

# 2. 启动数据库和缓存（首次需要拉取镜像，约2分钟）
docker-compose -f docker/docker-compose.yml up -d

# 3. 等待MySQL初始化完成（首次启动约30秒）
timeout /t 30

# 4. 启动后端（新开一个终端）
cd apps/server
mvn spring-boot:run

# 5. 启动前端（新开一个终端）
cd apps/web
pnpm install
pnpm dev
```

### Mac/Linux 用户

```bash
# 1. 进入项目目录
cd chainpass

# 2. 启动数据库和缓存
docker-compose -f docker/docker-compose.yml up -d

# 3. 等待MySQL初始化完成
sleep 30

# 4. 启动后端（新开一个终端）
cd apps/server
mvn spring-boot:run

# 5. 启动前端（新开一个终端）
cd apps/web
pnpm install
pnpm dev
```

---

## 三、访问系统

启动成功后：

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端页面** | http://localhost:5173 | Vue 3 前端 |
| **后端API** | http://localhost:8080 | Spring Boot |
| **健康检查** | http://localhost:8080/actuator/health | 后端状态 |

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | 超级管理员 |

---

## 四、常见问题排查

### 1. Docker 服务启动失败

```bash
# 查看日志
docker-compose -f docker/docker-compose.yml logs

# 重启服务
docker-compose -f docker/docker-compose.yml restart

# 完全重置
docker-compose -f docker/docker-compose.yml down -v
docker-compose -f docker/docker-compose.yml up -d
```

### 2. 后端启动失败

```bash
# 检查Java版本（需要17+）
java -version

# 检查端口占用
# Windows
netstat -ano | findstr :8080
# Mac/Linux
lsof -i :8080

# 清理Maven缓存重试
cd apps/server
mvn clean
mvn spring-boot:run
```

### 3. 前端启动失败

```bash
# 清理依赖重装
cd apps/web
rm -rf node_modules
pnpm install
pnpm dev
```

### 4. 数据库连接失败

```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 手动测试连接
docker exec -it chainpass-mysql mysql -uroot -pchainpass123

# 在MySQL中执行
SHOW DATABASES;
USE chainpass;
SHOW TABLES;
```

### 5. 登录后提示401错误

```
检查步骤：
1. 确保后端正在运行（访问 http://localhost:8080/actuator/health）
2. 打开浏览器开发者工具（F12）
3. 检查 Network 标签页中的请求
4. 确认请求头包含 Authorization: Bearer {token}
```

---

## 五、停止服务

```bash
# 停止前端/后端
# 在对应的终端按 Ctrl+C

# 停止Docker服务
docker-compose -f docker/docker-compose.yml down

# 停止并删除数据卷（完全清理）
docker-compose -f docker/docker-compose.yml down -v
```

---

## 六、生产部署

### 1. 构建生产版本

```bash
# 前端构建
cd apps/web
pnpm build
# 产物在 apps/web/dist 目录

# 后端构建
cd apps/server
mvn clean package -DskipTests
# 产物在 apps/server/target 目录
```

### 2. Docker 部署

```bash
# 构建镜像
docker build -t chainpass-web:latest ./apps/web
docker build -t chainpass-server:latest ./apps/server

# 运行容器
docker run -d -p 80:80 chainpass-web:latest
docker run -d -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/chainpass \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=chainpass123 \
  -e JWT_SECRET=your-secret-key \
  chainpass-server:latest
```

---

## 七、项目结构速览

```
chainpass/
├── apps/
│   ├── web/           # 前端源码
│   │   └── src/
│   │       ├── views/   # 页面组件
│   │       ├── api/     # API请求
│   │       └── stores/  # 状态管理
│   │
│   └── server/        # 后端源码
│       └── src/main/java/com/chainpass/
│           ├── controller/  # 接口层
│           ├── service/     # 业务层
│           └── entity/      # 实体类
│
└── docker/            # Docker配置
    ├── docker-compose.yml
    └── mysql/init.sql   # 数据库初始化
```

---

## 八、快速命令参考

```bash
# Docker
docker-compose -f docker/docker-compose.yml up -d      # 启动
docker-compose -f docker/docker-compose.yml down       # 停止
docker-compose -f docker/docker-compose.yml logs -f    # 查看日志

# 后端
cd apps/server && mvn spring-boot:run                  # 开发运行
cd apps/server && mvn test                             # 运行测试
cd apps/server && mvn package -DskipTests              # 打包

# 前端
cd apps/web && pnpm dev                                # 开发运行
cd apps/web && pnpm build                              # 构建
cd apps/web && pnpm type-check                         # 类型检查
```

---

**详细文档请查看**: [docs/PROJECT_GUIDE.md](./PROJECT_GUIDE.md)