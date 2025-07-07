# Spring Boot AOP 事务演示项目

本项目旨在演示如何通过 Spring AOP 为特定规则命名的服务类（例如所有 `*ServiceImpl` 类）自动启用事务管理，并验证事务回滚行为。

## 核心功能

-   **AOP 自动事务管理**：通过 Spring AOP 配置，为所有 `com.example` 包下以 `ServiceImpl` 结尾的类自动应用 `PROPAGATION_REQUIRED` 事务传播行为。这意味着您无需在每个 `ServiceImpl` 类上手动添加 `@Transactional` 注解。
-   **事务回滚演示**：通过模拟业务异常，演示了在 AOP 事务管理下，当方法抛出 `RuntimeException` 时，数据库操作能够正确回滚，保证数据一致性。

## 主要测试结果

项目包含 `ProductTransactionIntegrationTest`，用于验证 AOP 事务管理是否按预期工作。测试场景如下：

1.  调用 `ProductServiceImpl` 中的一个方法，该方法会执行数据库更新操作，并随后抛出一个 `RuntimeException`。
2.  由于 `ProductServiceImpl` 被 AOP 事务管理，当 `RuntimeException` 抛出时，Spring 会自动回滚该方法所做的所有数据库更改。
3.  测试断言数据库中的数据保持不变，从而验证了事务回滚的有效性。

**测试输出示例（精简）：**

```
[INFO] Running com.example.ProductTransactionIntegrationTest
Initial product name in test: InitialProductName
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: X.XXX s
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

## 如何运行

1.  **克隆项目：**
    ```bash
    git clone <项目仓库地址>
    cd <项目目录>
    ```
2.  **构建并运行测试：**
    ```bash
    mvn clean install
    mvn test
    ```
    您将在控制台输出中看到测试结果，确认 AOP 事务管理是否成功。

## 注意事项

-   本项目使用 H2 内存数据库，测试数据会在每次运行测试时初始化。
-   `ProductServiceTestInvoker.java` 是一个辅助类，用于解决 Spring AOP 中的“自我调用”问题，确保测试能够正确触发 AOP 代理。