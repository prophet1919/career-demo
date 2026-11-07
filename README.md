# career-demo

一个基于 Spring Boot 3 的 Java 后端练手项目，实现了统一返回体、全局异常处理、参数校验与分页查询，用于实践企业级后端接口的开发规范。

## 技术栈

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| Spring Boot | 3.5.16 | 主框架 |
| Java | 17 | 运行环境 |
| Spring Web | 随 Boot | REST 接口 |
| Spring Validation | 随 Boot | 参数校验 |
| Lombok | 随 Boot | 简化样板代码 |
| JUnit 5 + MockMvc | 随 Boot | 接口单元测试 |
| Maven Wrapper | 3.10.0 | 构建工具（无需本地安装 Maven） |

## 接口清单

所有接口统一返回 `Result<T>` 结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

`code = 0` 表示成功；非 0 表示业务错误（400 参数非法、404 资源不存在、500 服务器异常）。HTTP 状态码统一为 200，业务状态由 `code` 字段承载。

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/health` | 无 | 健康检查，返回服务状态与服务器时间戳 |
| GET | `/users` | `page` 页码，默认 1，最小 1<br>`size` 每页条数，默认 10，范围 1–100 | 分页查询用户列表 |
| GET | `/users/{id}` | `id` 用户 ID（路径参数） | 按 ID 查询用户，不存在返回 code=404 |

## 快速开始

需要 JDK 17。项目自带 Maven Wrapper，无需额外安装 Maven。

```bash
# 启动服务（默认端口 8080）
./mvnw spring-boot:run

# Windows CMD / PowerShell 使用
.\mvnw.cmd spring-boot:run

# 运行全部测试
./mvnw test
```

## 调用示例

```bash
# 健康检查
curl http://localhost:8080/health

# 分页查询：第 1 页，每页 1 条
curl "http://localhost:8080/users?page=1&size=1"

# 按 ID 查询用户
curl http://localhost:8080/users/1
```

### 错误场景

```bash
# size 超过上限 100 → code=400
curl "http://localhost:8080/users?page=1&size=200"
# {"code":400,"message":"...","data":null}

# 查询不存在的用户 → code=404
curl http://localhost:8080/users/999
# {"code":404,"message":"用户不存在","data":null}
```

## 项目结构

```
src/main/java/com/wzu/demo/
├── CareerDemoApplication.java   # 启动类
├── Result.java                  # 统一返回体
├── BizException.java            # 业务异常
├── GlobalExceptionHandler.java  # 全局异常处理
├── HealthController.java        # 健康检查接口
└── UserController.java          # 用户接口

src/test/java/com/wzu/demo/
├── CareerDemoApplicationTests.java  # 上下文加载测试
└── UserControllerTest.java          # 用户接口 MockMvc 测试
```

## 已实现的后端规范

- **统一返回体**：`Result<T>` 固定 code / message / data 三段结构，前后端约定统一，前端可集中拦截处理
- **全局异常处理**：`@RestControllerAdvice` 拦截业务异常（`BizException`）、参数校验异常（`ConstraintViolationException`）与未知异常，避免异常堆栈直接暴露给调用方
- **参数校验**：`@Validated` + `@Min` / `@Max` 在接口层拦截非法入参，校验失败统一返回 code=400
- **分页查询**：支持 page / size 分页，页码越界时返回空列表而非报错
- **接口测试**：MockMvc 覆盖「正常分页」「参数越界」「资源不存在」三个场景，`./mvnw test` 一键回归