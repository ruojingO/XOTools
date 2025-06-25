package com.example.webapp;

import com.example.common.SharedUtil;

public class MyClassInWebApp {

    public void demonstrateClassLoaders() {
        System.out.println("--- Inside MyClassInWebApp ---");

        ClassLoader clNonCompliant = this.getClass().getClassLoader();
        ClassLoader clCompliant = Thread.currentThread().getContextClassLoader();

        System.out.println("MyClassInWebApp - MyClassInWebApp's ClassLoader (this.getClass().getClassLoader()): " + clNonCompliant);
        System.out.println("MyClassInWebApp - MyClassInWebApp's Class Name: " + this.getClass().getName());
        System.out.println("MyClassInWebApp - Thread Context ClassLoader (Thread.currentThread().getContextClassLoader()): " + clCompliant);

        if (clNonCompliant == clCompliant) {
            System.out.println("MyClassInWebApp: For MyClassInWebApp itself, this.getClass().getClassLoader() and Thread.currentThread().getContextClassLoader() are THE SAME.");
        } else {
            System.out.println("MyClassInWebApp: For MyClassInWebApp itself, this.getClass().getClassLoader() and Thread.currentThread().getContextClassLoader() are DIFFERENT!");
        }

        System.out.println("MyClassInWebApp - Parent of this.getClass().getClassLoader(): " + (clNonCompliant != null ? clNonCompliant.getParent() : "null"));
        System.out.println("MyClassInWebApp - Parent of Thread.currentThread().getContextClassLoader(): " + (clCompliant != null ? clCompliant.getParent() : "null"));
        System.out.println("---");

        // Attempt to load a resource from the webapp's perspective using both classloaders
        String webAppResource = "com/example/webapp/webapp-specific-resource.txt"; // Conceptual resource

        if (clNonCompliant != null && clNonCompliant.getResource(webAppResource) != null) {
            System.out.println("MyClassInWebApp: Webapp resource loaded successfully using this.getClass().getClassLoader().");
        } else {
            System.out.println("MyClassInWebApp: Webapp resource NOT found using this.getClass().getClassLoader().");
        }

        if (clCompliant != null && clCompliant.getResource(webAppResource) != null) {
            System.out.println("MyClassInWebApp: Webapp resource loaded successfully using Thread.currentThread().getContextClassLoader().");
        } else {
            System.out.println("MyClassInWebApp: Webapp resource NOT found using Thread.currentThread().getContextClassLoader().");
        }
        System.out.println("---");

        // Now, let's use SharedUtil
        // In a real Tomcat scenario where SharedUtil.jar is in tomcat/lib and MyClassInWebApp is in a WAR,
        // SharedUtil will be loaded by a parent classloader (e.g., CommonClassLoader).
        SharedUtil util = new SharedUtil();
        System.out.println("--- Calling SharedUtil from MyClassInWebApp ---");
        util.printClassLoaderInfo("SharedUtil called from MyClassInWebApp");
    }

    // Dummy method to create a conceptual resource for testing getResource
    public static void main(String[] args) {
        // This main is just for basic standalone checking, not for the Tomcat scenario.
        MyClassInWebApp instance = new MyClassInWebApp();
        instance.demonstrateClassLoaders();
    }
}
