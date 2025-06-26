package com.example.externallib;

/**
 * ExternalClass 是一个简单的Java类，用于演示在Tomcat环境中，
 * 当一个类由父级类加载器加载时，其自身的类加载器和线程上下文类加载器可能不同。
 * 这个类应该被打包成JAR并放置在Tomcat的lib目录下。
 */
public class ExternalClass {

    /**
     * 获取并返回当前类（ExternalClass）的类加载器信息和当前线程的上下文类加载器信息。
     * @return 包含类加载器信息的字符串。
     */
    public static String getClassLoadersInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- 在 ExternalClass 内部 (由Tomcat的父级类加载器加载) ---\n");

        // 获取加载当前类（ExternalClass）的类加载器
        // 由于ExternalClass被放置在Tomcat的lib目录下，它通常会被Tomcat的Common或Shared类加载器加载。
        ClassLoader classLoaderOfThisClass = ExternalClass.class.getClassLoader();
        sb.append("  ExternalClass 的类加载器: ").append(classLoaderOfThisClass).append("\n");

        // 获取当前线程的上下文类加载器
        // 在Web应用中，这通常是Web应用的WebappClassLoader。
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        sb.append("  线程上下文类加载器: ").append(contextClassLoader).append("\n");

        // 比较两者是否是同一个实例
        // 在这种情况下，它们通常会是不同的实例，从而重现S3032规则所警告的问题。
        boolean areSame = (classLoaderOfThisClass == contextClassLoader);
        sb.append("  它们是同一个实例吗？ ").append(areSame).append("\n");

        sb.append("----------------------------------------------------\n");
        return sb.toString();
    }
}