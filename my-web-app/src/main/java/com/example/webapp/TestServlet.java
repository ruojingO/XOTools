package com.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TestServlet 是一个用于演示Java Web应用中类加载器行为的Servlet。
 * 它特别关注SonarQube S3032规则（不应使用getClass().getClassLoader()）所涉及的问题。
 * 当部署到Tomcat等Servlet容器时，可以通过访问 /my-web-app/testClassLoaders 来触发其逻辑。
 */
@WebServlet("/testClassLoaders")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * 处理GET请求，并输出类加载器信息。
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain"); // 设置响应内容类型为纯文本
        PrintWriter out = response.getWriter(); // 获取用于向客户端写入响应的PrintWriter

        out.println("--- 类加载器测试Servlet ---");
        out.println("此Servlet演示了在JEE环境中获取类加载器的正确和错误方式之间的区别。");
        out.println();

        // --- 不合规方法 (SonarQube S3032 规则) ---
        // 使用this.getClass().getClassLoader()获取类加载器。
        // 在某些JEE环境中，这可能无法提供正确的类加载上下文。
        ClassLoader clNonCompliant = getClassLoaderNonCompliant();
        String nonCompliantResult = "1. 不合规方法 (this.getClass().getClassLoader()): " + clNonCompliant;
        out.println(nonCompliantResult);
        System.out.println(nonCompliantResult); // 同时输出到服务器日志

        // --- 合规方法 ---
        // 使用Thread.currentThread().getContextClassLoader()获取类加载器。
        // 这是在JEE环境中获取类加载器的推荐方式，因为它由容器设置，能提供正确的上下文。
        ClassLoader clCompliant = getClassLoaderCompliant();
        String compliantResult = "2. 合规方法 (Thread.currentThread().getContextClassLoader()): " + clCompliant;
        out.println(compliantResult);
        System.out.println(compliantResult); // 同时输出到服务器日志

        out.println();

        // --- 比较 ---
        // 比较两种方法获取的类加载器是否是同一个实例。
        // 在简单的Web应用中，它们可能相同；但在更复杂的场景（如本例中调用外部库）下，它们可能不同。
        boolean areSame = (clNonCompliant == clCompliant);
        String comparisonResult = "它们是同一个实例吗？ " + areSame;
        out.println(comparisonResult);
        System.out.println(comparisonResult);

        out.println();
        out.println("在Tomcat环境中，这些可能不同。'合规方法'是正确的使用方式。");

        // --- 调用ExternalClass来演示问题 ---
        // ExternalClass被放置在Tomcat的lib目录下，由Tomcat的父级类加载器加载。
        // 调用其方法将展示当一个类由父级加载器加载，但其内部获取上下文类加载器时，两者可能不同。
        out.println(com.example.externallib.ExternalClass.getClassLoadersInfo());
        System.out.println(com.example.externallib.ExternalClass.getClassLoadersInfo());
    }

    /**
     * 不合规方法：直接使用当前类的类加载器。
     * 根据SonarQube规则java:S3032，这种方式在JEE环境中是不推荐的。
     * @return 加载当前类的ClassLoader实例。
     */
    private ClassLoader getClassLoaderNonCompliant() {
        return this.getClass().getClassLoader();
    }

    /**
     * 合规方法：使用当前线程的上下文类加载器。
     * 这是在JEE环境中获取类加载器的推荐方式。
     * @return 当前线程的上下文ClassLoader实例。
     */
    private ClassLoader getClassLoaderCompliant() {
        return Thread.currentThread().getContextClassLoader();
    }
}