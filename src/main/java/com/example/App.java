package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserServiceWithoutTx userServiceWithoutTx;

    @Autowired
    private UserServiceWithTx userServiceWithTx;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化数据库
        jdbcTemplate.execute("DROP TABLE IF EXISTS app_user");
        jdbcTemplate.execute("CREATE TABLE app_user(id INT PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.update("INSERT INTO app_user(id, name) VALUES(1, '初始名称')");

        System.out.println("\n--- 无事务场景演示 ---");
        System.out.println("更新前用户名称: " + jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));
        try {
            userServiceWithoutTx.updateNameWithoutTx(1L, "无事务更新名称");
        } catch (RuntimeException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }
        System.out.println("更新后用户名称 (无事务): " + jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));

        // 恢复数据
        jdbcTemplate.update("UPDATE app_user SET name='初始名称' WHERE id=1");

        System.out.println("\n--- 有事务场景演示 ---");
        System.out.println("更新前用户名称: " + jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));
        try {
            userServiceWithTx.updateNameWithTx(1L, "有事务更新名称");
        } catch (RuntimeException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }
        System.out.println("更新后用户名称 (有事务): " + jdbcTemplate.queryForObject("SELECT name FROM app_user WHERE id = 1", String.class));
    }
}