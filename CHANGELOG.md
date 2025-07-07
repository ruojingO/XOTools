# 更改日志

## [1.1.0] - 2025-07-07

### 新增
- 为所有 `*ServiceImpl` 类实现了 AOP 事务自动启用功能。
- 添加了 `ProductService` 和 `ProductServiceImpl` 用于 AOP 事务测试。
- 添加了 `ProductServiceTestInvoker` 辅助类，用于解决 AOP 自我调用问题，确保测试的正确性。
- 添加了 `ProductTransactionIntegrationTest` 集成测试，验证 AOP 事务回滚功能。

### 更改
- 重命名了核心类和测试类，使其名称更具描述性和友好性：
  - `App.java` -> `TransactionDemoApplication.java`
  - `UserServiceWithoutTx.java` -> `NoTransactionUserService.java`
  - `UserServiceWithTx.java` -> `TransactionalUserService.java`
  - `TransactionAopConfig.java` -> `AopTransactionConfig.java`
  - `ProductServiceImplTest.java` -> `ProductTransactionIntegrationTest.java`
  - `ProductTestHelperImpl.java` -> `ProductServiceTestInvoker.java`
  - `AppTest.java` -> `TransactionDemoApplicationTest.java`
- 优化了代码中的字符串字面量，使其更生动、更易于理解。
- 清理了 `pom.xml` 中重复的 `h2database` 依赖。

## [1.0.0] - 2025-07-07

### 新增
- 添加了事务演示代码，包括 `UserServiceWithoutTx` 和 `UserServiceWithTx` 类，用于演示 Spring `@Transactional` 的作用。
- 添加了 `README.md` 文件，详细说明了项目、代码示例、运行方式和事务对比。
- 添加了 `.gitignore` 文件，忽略 `target/` 和 `.idea/` 目录，避免提交构建产物和 IDE 配置。

### 更改
- 更新了 `pom.xml` 以支持新的服务类。
- 更新了 `App.java` 和 `AppTest.java` 以集成和测试事务演示。