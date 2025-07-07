package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Sql("/test-product.sql")
class ProductTransactionIntegrationTest {

    @Autowired
    private ProductServiceTestInvoker productTestInvoker;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void updateProductNameWithTx() {
        String initialName = jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, 1L);
        System.out.println("Initial product name in test: " + initialName);

        assertThrows(RuntimeException.class, () -> productTestInvoker.testUpdate());

        // Now, we verify that the transaction was rolled back.
        String name = jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, 1L);
        assertEquals("InitialProductName", name, "The name should have been rolled back to InitialProductName");
    }
}
