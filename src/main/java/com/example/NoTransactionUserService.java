package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class NoTransactionUserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateNameWithoutTx(Long id, String name) {
        // 1. 执行更新，自动提交（auto-commit=true）
        jdbcTemplate.update("UPDATE app_user SET name=? WHERE id=?", name, id);
        // 2. 模拟后续业务异常
        throw new RuntimeException("模拟异常：更新后发生错误");
    }
}
