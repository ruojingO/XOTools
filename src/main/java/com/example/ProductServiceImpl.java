package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void updateProductName(Long id, String name) {
        jdbcTemplate.update("UPDATE product SET name = ? WHERE id = ?", name, id);
        throw new RuntimeException("Rollback test");
    }
}
