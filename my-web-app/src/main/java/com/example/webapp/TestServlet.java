package com.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/testClassLoaders")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        out.println("Starting classloader demonstration... Check server logs for detailed output.");
        System.out.println("=== TestServlet doGet: Triggering classloader demonstration ===");

        MyClassInWebApp myClass = new MyClassInWebApp();
        myClass.demonstrateClassLoaders();

        System.out.println("=== TestServlet doGet: Classloader demonstration complete ===");
        out.println("Classloader demonstration triggered. Check server logs.");
    }
}
