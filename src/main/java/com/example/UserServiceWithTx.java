package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional  // 默认 readOnly=false, PROPAGATION_REQUIRED
public class UserServiceWithTx {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updateNameWithTx(Long id, String name) {
        // 1. 执行更新，暂不提交（auto-commit=false）
        jdbcTemplate.update("UPDATE app_user SET name=? WHERE id=?", name, id);
        // 2. 模拟后续业务异常
        throw new RuntimeException("模拟异常：更新后发生错误");
    }
}
