package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceTestInvoker {

    @Autowired
    private ProductService productService;

    public void testUpdate() {
        productService.updateProductName(1L, "UpdatedProductName");
    }
}
