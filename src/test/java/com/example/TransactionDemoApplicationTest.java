package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TransactionDemoApplicationTest {

    @Autowired
    private NoTransactionUserService noTransactionUserService;

    @Autowired
    private TransactionalUserService transactionalUserService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM app_user");
        jdbcTemplate.update("INSERT INTO app_user (id, name) VALUES (1, 'OriginalName');");
    }

    @Test
    void testUpdateNameWithoutTx() {
        // 初始状态
        assertEquals("OriginalName", jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));

        // 执行无事务更新，预期抛出异常
        assertThrows(RuntimeException.class, () -> noTransactionUserService.updateNameWithoutTx(1L, "UpdatedNameWithoutTx"));

        // 检查数据：无事务，数据应该已经更新
        assertEquals("UpdatedNameWithoutTx", jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));
    }

    @Test
    void testUpdateNameWithTx() {
        // 初始状态
        assertEquals("OriginalName", jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));

        // 执行有事务更新，预期抛出异常
        assertThrows(RuntimeException.class, () -> transactionalUserService.updateNameWithTx(1L, "UpdatedNameWithTx"));

        // 检查数据：有事务，数据应该回滚到原始状态
        assertEquals("OriginalName", jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));
    }
}
