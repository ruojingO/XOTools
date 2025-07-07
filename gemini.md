need use chinese reply



pls base now branch create a new branch: testTranscation

my requirement is below, u can import a memory db to vivid to demo the differ 



~~~markdown
- 目录  
  - [1. 演示概述](#1-演示概述)  
  - [2. 代码示例](#2-代码示例)  
    - [2.1 无事务场景](#21-无事务场景)  
    - [2.2 有事务场景](#22-有事务场景)  
  - [3. 运行结果与对比](#3-运行结果与对比)  
  - [4. 小结](#4-小结)  

---

## 1. 演示概述  
本示例通过对比“无事务”与“有事务”两种情况下，同一条 `UPDATE` 语句后抛出异常时数据是否回滚，来说明 `@Transactional` 的作用。

---

## 2. 代码示例

### 2.1 无事务场景  
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
~~~

### 2.2 有事务场景

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

------

## 3. 运行结果与对比

1. **无事务方法 `updateNameWithoutTx`**
   - 执行完 `UPDATE` 后立即提交，数据已修改；
   - 随后抛出异常时，该更新无法回滚，数据库中依然保留修改结果。
2. **有事务方法 `updateNameWithTx`**
   - 在方法结束前未提交；
   - 抛出 `RuntimeException` 时，Spring 拦截并对整个事务回滚，更新操作被撤销，数据保持原状。

------

## 4. 小结

- 单条写操作在 **无事务（auto-commit）** 下同样会“成功或失败”，但一旦执行，后续异常无法撤销；
- 加上 `@Transactional` 后，写操作会被纳入方法级事务，遇异常可统一回滚，保证业务逻辑的原子性与数据一致性。

```

```