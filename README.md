# 事务演示项目

本示例项目旨在通过对比“无事务”与“有事务”两种情况下，同一条 `UPDATE` 语句后抛出异常时数据是否回滚，来说明 Spring `@Transactional` 注解的作用。

## 目录
- [1. 项目概述](#1-项目概述)
- [2. 代码示例](#2-代码示例)
  - [2.1 无事务场景](#21-无事务场景)
  - [2.2 有事务场景](#22-有事务场景)
- [3. 运行结果与对比](#3-运行结果与对比)
- [4. 如何运行](#4-如何运行)
- [5. 小结](#5-小结)

---

## 1. 项目概述
本项目是一个简单的 Maven 项目，包含两个服务类：`UserServiceWithoutTx` 和 `UserServiceWithTx`，分别演示了在没有事务和有事务的情况下，数据库操作遇到异常时的行为。

---

## 2. 代码示例

### 2.1 无事务场景
`UserServiceWithoutTx` 类中的 `updateNameWithoutTx` 方法不使用 `@Transactional` 注解。
```java
@Service
public class UserServiceWithoutTx {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateNameWithoutTx(Long id, String name) {
        // 1. 执行更新，自动提交（auto-commit=true）
        jdbcTemplate.update("UPDATE user SET name=? WHERE id=?", name, id);
        // 2. 模拟后续业务异常
        throw new RuntimeException("模拟异常：更新后发生错误");
    }
}
```
**说明：** 在此场景下，`jdbcTemplate.update` 执行后会立即提交，即使后续抛出异常，之前的更新也无法回滚。

### 2.2 有事务场景

`UserServiceWithTx` 类中的 `updateNameWithTx` 方法使用了 `@Transactional` 注解。
```java
@Service
@Transactional  // 默认 readOnly=false, PROPAGATION_REQUIRED
public class UserServiceWithTx {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateNameWithTx(Long id, String name) {
        // 1. 执行更新，暂不提交（auto-commit=false）
        jdbcTemplate.update("UPDATE user SET name=? WHERE id=?", name, id);
        // 2. 模拟后续业务异常
        throw new RuntimeException("模拟异常：更新后发生错误");
    }
}
```
**说明：** `@Transactional` 注解使得整个方法在一个事务中运行。当 `RuntimeException` 抛出时，Spring 会拦截异常并回滚整个事务，确保数据的一致性。

---

## 3. 运行结果与对比

1.  **无事务方法 `updateNameWithoutTx`**
    *   执行完 `UPDATE` 后立即提交，数据已修改；
    *   随后抛出异常时，该更新无法回滚，数据库中依然保留修改结果。
2.  **有事务方法 `updateNameWithTx`**
    *   在方法结束前未提交；
    *   抛出 `RuntimeException` 时，Spring 拦截并对整个事务回滚，更新操作被撤销，数据保持原状。

---

## 4. 如何运行

1.  **克隆项目：**
    ```bash
    git clone <项目仓库地址>
    cd <项目目录>
    ```
2.  **构建项目：**
    ```bash
    mvn clean install
    ```
3.  **运行测试：**
    项目中的 `AppTest.java` 包含了对这两个场景的测试用例，可以直接运行 Maven 测试来观察结果。
    ```bash
    mvn test
    ```
    您将在控制台输出中看到两个场景的对比结果。

---

## 5. 小结

-   单条写操作在 **无事务（auto-commit）** 下同样会“成功或失败”，但一旦执行，后续异常无法撤销；
-   加上 `@Transactional` 后，写操作会被纳入方法级事务，遇异常可统一回滚，保证业务逻辑的原子性与数据一致性。
