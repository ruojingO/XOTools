# 更改日志

## [1.0.0] - 2025-07-07

### 新增
- 添加了事务演示代码，包括 `UserServiceWithoutTx` 和 `UserServiceWithTx` 类，用于演示 Spring `@Transactional` 的作用。
- 添加了 `README.md` 文件，详细说明了项目、代码示例、运行方式和事务对比。
- 添加了 `.gitignore` 文件，忽略 `target/` 和 `.idea/` 目录，避免提交构建产物和 IDE 配置。

### 更改
- 更新了 `pom.xml` 以支持新的服务类。
- 更新了 `App.java` 和 `AppTest.java` 以集成和测试事务演示。
