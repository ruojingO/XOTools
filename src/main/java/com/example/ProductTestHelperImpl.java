package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductTestHelperImpl {

    @Autowired
    private ProductService productService;

    public void testUpdate() {
        try {
            // This call is now on a different bean, so the AOP proxy will work as expected.
            productService.updateProductName(1L, "NewName");
        } catch (Exception e) {
            // The exception is caught so the test method can continue and assert the rollback.
        }
    }
}
