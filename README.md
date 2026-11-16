# career-demo

一个基于 **Spring Boot 3.5 + Java 17** 的后端实践项目，目标是完整走一遍企业级接口的开发规范。

覆盖：统一返回体、全局异常处理、参数校验、请求日志链路追踪（traceId）、JWT 无状态鉴权、BCrypt 密码加密、基于注解的角色权限、MySQL 数据源接入。

> 定位：不是业务系统，是**工程规范样板**。当前用户数据仍存内存，下一步迁移到 MySQL。

---

## 技术栈

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 3.5.16 | 主框架 |
| Java | 17 | 运行环境（Spring Boot 3.x 最低要求） |
| Spring Web | 随 Boot | REST 接口 |
| Spring Validation | 随 Boot | 注解式参数校验（`jakarta.validation`） |
| Spring Security Crypto | 6.5.x | BCrypt 密码加密（仅引入 crypto 模块，未启用整套 Security） |
| jjwt | 0.12.6 | JWT 签发与校验（HS256） |
| MySQL | 8.0.26 | 数据源（本地开发） |
| Spring JDBC | 随 Boot | JdbcTemplate，当前用于连通性验证 |
| JUnit 5 + MockMvc | 随 Boot | 接口单元测试 |
| Maven Wrapper | 3.10.0 | 构建工具（无需本地安装 Maven） |

> 注意：依赖了 Lombok，但当前所有实体与 DTO 均为手写 getter/setter，未使用 Lombok 生成。

---

## 接口清单

所有接口统一返回 `Result<T>` 结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

`code = 0` 表示成功；非 0 表示业务错误：

| code | 含义 |
| --- | --- |
| 400 | 参数非法（校验失败） |
| 401 | 未登录、token 缺失/失效/过期 |
| 403 | 权限不足（角色不匹配） |
| 404 | 资源不存在 |
| 409 | 用户名已存在 |
| 500 | 服务器内部异常 |

业务状态主要由 `code` 字段承载，HTTP 层统一返回 200。

> **鉴权说明**：`/users`、`/users/{id}`、`/api/admin/**` 需要请求头 `Authorization: Bearer <token>`；
> `/health`、`/api/auth/**`、`/api/db/**` 公开访问。

| 方法 | 路径 | 参数 | 说明 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/health` | 无 | 健康检查，返回服务状态与服务器时间戳 | 否 |
| GET | `/users` | `page` 页码，默认 1，最小 1<br>`size` 每页条数，默认 10，范围 1–100 | 分页查询用户列表 | 是（任意角色） |
| GET | `/users/{id}` | `id` 用户 ID（路径参数） | 按 ID 查询用户，不存在返回 code=404 | 是（任意角色） |
| POST | `/api/auth/register` | JSON：`username`（3–20 字）、`password`（6–32 字） | 注册，密码 BCrypt 加密存储，返回新用户 id；重名返回 code=409 | 否 |
| POST | `/api/auth/login` | JSON：`username`、`password` | 登录，成功返回 JWT（有效期 2 小时）；用户名不存在与密码错误**统一**返回 code=401 | 否 |
| GET | `/api/admin/users` | Header：`Authorization` | 管理员查看全部用户，需 `ADMIN` 角色，否则 code=403 | 是（ADMIN） |
| GET | `/api/db/ping` | 无 | **临时验证接口**：返回 MySQL 版本与连通状态 | 否 |
| GET | `/api/db/users` | 无 | **临时验证接口**：直接查 `user` 表 | 否 |

---

## 快速开始

### 环境要求

- JDK 17
- MySQL 8（仅 `/api/db/**` 需要；不启动 MySQL 时其余接口正常工作）

### 1. 建库建表

```sql
CREATE DATABASE career_demo DEFAULT CHARSET utf8mb4;

USE career_demo;

CREATE TABLE `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,   -- BCrypt 哈希为 60 字符，务必留足长度
  role VARCHAR(10) NOT NULL DEFAULT 'USER',
  create_time BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

> `password` 字段必须 `VARCHAR(100)` 以上。BCrypt 输出 60 字符，用 `VARCHAR(32)` 会被静默截断，导致登录永远失败。

### 2. 启动服务

项目自带 Maven Wrapper，无需额外安装 Maven。

```bash
# macOS / Linux / Git Bash
./mvnw spring-boot:run

# Windows CMD
mvnw.cmd spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

默认端口 8080。启动成功后访问 `http://localhost:8080/health`。

### 3. 运行测试

```bash
./mvnw test        # Windows: mvnw.cmd test
```

预期输出：`Tests run: 6, Failures: 0, Errors: 0`。

---

## 配置说明

数据源账号密码与 JWT 密钥均走环境变量，不写死在配置文件里：

```properties
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:root}

jwt.secret=${JWT_SECRET:wzu-career-demo-secret-key-2026-please-change}
jwt.expire-hours=2
```

`${VAR:default}` 的语义是「优先读环境变量，读不到用冒号后的默认值」。

- **本地开发**：直接用默认值即可
- **部署时**：通过环境变量 `DB_USER` / `DB_PASSWORD` / `JWT_SECRET` 覆盖

> 默认 JWT 密钥仅为本地调试用。**生产环境必须通过 `JWT_SECRET` 替换**，否则任何人都能用这个公开密钥伪造 token。

---

## 调用示例

### 1. 健康检查（无需鉴权）

```bash
curl http://localhost:8080/health
# {"code":0,"message":"ok","data":{"status":"ok","time":1794669897262}}
```

### 2. 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
# {"code":0,"message":"ok","data":3}
```

### 3. 登录拿 token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
# {"code":0,"message":"ok","data":"eyJhbGciOiJIUzI1NiJ9..."}
```

### 4. 带 token 访问受保护接口

```bash
curl "http://localhost:8080/users?page=1&size=2" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
# {"code":0,"message":"ok","data":[{"id":1,"username":"张三","role":"USER","createTime":...}]}
```

> 响应的 user 对象**不含 password 字段** —— `User.password` 加了 `@JsonIgnore`。

### 5. 管理员接口

```bash
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <ADMIN 角色的 token>"
# {"code":0,"message":"ok","data":[...]}
```

### 6. 数据库连通性

```bash
curl http://localhost:8080/api/db/ping
# {"code":0,"message":"ok","data":{"connected":true,"mysql":"8.0.26"}}
```

### Windows CMD 注意事项

CMD 下 `curl` 与 `curl.exe` 等价，但 JSON 里的双引号需要转义：

```cmd
curl.exe -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"123456\"}"
```

PowerShell 5.1 会吃掉传给 `.exe` 的双引号，建议改用 CMD 终端或写成 `-d '{\"username\":\"...\"}'`。

---

## 错误场景

```bash
# 参数越界 → code=400
curl "http://localhost:8080/users?page=1&size=200" -H "Authorization: Bearer <token>"
# {"code":400,"message":"size: must be less than or equal to 100","data":null}

# 用户不存在 → code=404
curl http://localhost:8080/users/999 -H "Authorization: Bearer <token>"
# {"code":404,"message":"用户不存在","data":null}

# 不带 token → code=401
curl http://localhost:8080/users
# {"code":401,"message":"未登录或 token 缺失","data":null}

# token 伪造或过期 → code=401
curl http://localhost:8080/users -H "Authorization: Bearer abc.def.ghi"
# {"code":401,"message":"token 无效或已过期","data":null}

# 非 ADMIN 访问管理接口 → code=403
curl http://localhost:8080/api/admin/users -H "Authorization: Bearer <USER 角色的 token>"
# {"code":403,"message":"权限不足，需要 ADMIN","data":null}

# 用户名重复 → code=409
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
# {"code":409,"message":"用户名已存在","data":null}
```

---

## 项目结构

```
src/main/java/com/wzu/demo/
├── CareerDemoApplication.java   # 启动类
├── Result.java                  # 统一返回体 Result<T>（code / message / data）
├── BizException.java            # 业务异常，携带自定义 code
├── GlobalExceptionHandler.java  # 全局异常处理（4 个 handler）
├── WebLogFilter.java            # 请求日志过滤器：MDC 注入 traceId，记录耗时
├── WebConfig.java               # MVC 配置：注册拦截器与受保护路径
├── AuthInterceptor.java         # JWT 校验拦截器 + @RequireRole 角色检查
├── RequireRole.java             # 角色注解（RUNTIME 保留，供反射读取）
├── TokenService.java            # JWT 签发与解析（密钥来自配置）
├── PasswordService.java         # BCrypt 加密封装（encode / matches）
├── HealthController.java        # GET  /health
├── UserController.java          # GET  /users, /users/{id}
├── AuthController.java          # POST /api/auth/register, /login
├── AdminController.java         # GET  /api/admin/users
├── DbCheckController.java       # GET  /api/db/ping, /api/db/users（临时）
├── User.java                    # 用户实体（password 带 @JsonIgnore）
└── UserService.java             # 用户数据层（当前 ConcurrentHashMap，待迁移 MySQL）

src/test/java/com/wzu/demo/
├── CareerDemoApplicationTests.java  # 上下文加载测试
└── UserControllerTest.java          # 6 个 MockMvc 用例（含 2 个鉴权负向）
```

> 当前所有类存放在同一个包。下一步迁移 MySQL 时按职责分包：`common/`（Result、异常、异常处理器）、`config/`（WebConfig、拦截器、过滤器）、`controller/`、`service/`、`entity/`、`mapper/`。

---

## 已实现的后端规范

- **统一返回体**：`Result<T>` 固定 code / message / data 三段结构，前端可集中拦截处理
- **全局异常处理**：`@RestControllerAdvice` 分类拦截业务异常、参数校验异常、请求体校验异常与未知异常，避免堆栈外泄。业务异常返回其自带 code，校验异常返回 400，未知异常返回 500 且不暴露细节
- **参数校验**：`@Validated` + `@Min` / `@Max` 在接口层拦截非法入参；请求体用 `@Valid` + `@NotBlank` / `@Size`
- **分页查询**：支持 page / size，页码越界返回空列表而非报错
- **请求日志与链路追踪**：`OncePerRequestFilter` 以最高优先级执行，为每个请求生成 8 位 traceId 注入 MDC，日志格式含 `%X{traceId}`，可据此串起一次请求的全部日志
- **JWT 无状态鉴权**：登录签发 HS256 token（有效期 2 小时，可配），`HandlerInterceptor` 校验签名与过期时间，解析出的 userId / role 放入 request attribute 供后续使用
- **角色权限**：`@RequireRole("ADMIN")` 注解标记受控方法或类，拦截器按 token 中的 role 校验，不匹配返回 403。新增受保护路径需同步修改 `WebConfig` 的 `addPathPatterns`
- **密码安全**：注册即 BCrypt 加盐哈希入库；实体 `password` 字段加 `@JsonIgnore` 防止序列化泄露；登录时用户名不存在与密码错误返回同一提示，避免用户名枚举
- **配置外置**：数据库密码与 JWT 密钥走环境变量，配置文件中只保留调试用默认值
- **接口测试**：MockMvc 覆盖正常分页、参数越界、资源不存在三个正向场景，以及无 token、错误 token 两个鉴权负向场景

---

## 设计取舍

几个刻意的选择，也是面试常问的点：

**1. 为什么业务异常不返回真实 HTTP 状态码？**

`BizException(401, ...)` 抛出去后，`GlobalExceptionHandler` 返回 `{"code":401}`，但 HTTP 层仍是 200。

好处是前端只需在 axios 拦截器里判断 `code`，不用同时处理 HTTP 层异常；代价是网关和监控抓不到错误率。

后续如果需要，可改为 `ResponseEntity.status(e.getCode()).body(...)`。这属于规范取舍，不是遗漏。

**2. 为什么用 HandlerInterceptor 而不是 Spring Security？**

当前只有 8 个接口、2 种角色，引入 Spring Security 会带来整条过滤器链、`SecurityContext`、默认登录页等大量隐式行为，成本高于收益。

等做到细粒度 RBAC（用户-角色-权限三表）时再迁移更合适。

**3. 为什么 `UserService` 还存内存？**

这是刻意的阶段性选择：先把手动 SQL 和连接池跑通（`/api/db/**` 已验证），再引入 MyBatis 替换存储层。

这样排查问题时能区分「是数据库连接问题」还是「是 ORM 映射问题」。

---

## 待办

- [ ] `UserService` 迁移到 MySQL（MyBatis），补齐 Mapper 层
- [ ] 分页返回 `total` 字段（前端分页需要）
- [ ] 按职责分包（common / config / controller / service / entity / mapper）
- [ ] Dockerfile + docker-compose 部署
- [ ] Swagger / Knife4j 接口文档
- [ ] `/api/db/**` 两个临时接口在迁移完成后移除
