package com.example;

/**
 * MyClass 是一个简单的Java类，用于演示类加载器在独立应用中的行为。
 * 它包含了两种获取类加载器的方法，一种是不合规的（根据SonarQube S3032规则），另一种是合规的。
 */
public class MyClass {

    /**
     * 不合规方法：直接使用当前类的类加载器。
     * 根据SonarQube规则java:S3032，这种方式在JEE环境中是不推荐的，因为它可能无法提供正确的类加载上下文。
     * @return 加载当前类的ClassLoader实例。
     */
    public ClassLoader getClassLoaderNonCompliant() {
        // 根据SonarQube规则java:S3032，这是不合规的用法
        return this.getClass().getClassLoader();
    }

    /**
     * 合规方法：使用当前线程的上下文类加载器。
     * 这是在JEE环境中获取类加载器的推荐方式，因为它由容器设置，能提供正确的上下文。
     * @return 当前线程的上下文ClassLoader实例。
     */
    public ClassLoader getClassLoaderCompliant() {
        // 合规的解决方案
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 主方法，用于在独立应用中运行并演示类加载器。
     * 在简单的独立应用中，两种方法获取的类加载器通常是相同的。
     * S3032规则所强调的问题主要发生在JEE环境中，那里的类加载器层次结构更为复杂。
     * @param args 命令行参数。
     */
    public static void main(String[] args) {
        MyClass instance = new MyClass();
        ClassLoader clNonCompliant = instance.getClassLoaderNonCompliant();
        ClassLoader clCompliant = instance.getClassLoaderCompliant();

        System.out.println("不合规的类加载器: " + clNonCompliant);
        System.out.println("合规的类加载器: " + clCompliant);

        // 在简单的独立应用程序中，这两个类加载器可能通常是相同的。
        // S3032规则强调的问题是针对JEE环境的，那里的类加载器层次结构可能更复杂。
        if (clNonCompliant != null && clCompliant != null) {
            System.out.println("在此上下文中它们是同一个吗？ " + (clNonCompliant == clCompliant));
        }
    }
}