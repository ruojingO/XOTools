package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Sql("/test-product.sql")
class ProductServiceImplTest {

    @Autowired
    private ProductTestHelperImpl productTestHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void updateProductNameWithTx() {
        // This call will be wrapped in its own transaction by our AOP configuration.
        productTestHelper.testUpdate();

        // Now, we verify that the transaction was rolled back.
        String name = jdbcTemplate.queryForObject("SELECT name FROM product WHERE id = ?", String.class, 1L);
        assertEquals("OriginalName", name, "The name should have been rolled back to OriginalName");
    }
}
