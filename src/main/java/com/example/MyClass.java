package com.example;

public class MyClass {

    public ClassLoader getClassLoaderNonCompliant() {
        // Noncompliant according to SonarQube rule java:S3032
        return this.getClass().getClassLoader();
    }

    public ClassLoader getClassLoaderCompliant() {
        // Compliant solution
        return Thread.currentThread().getContextClassLoader();
    }

    public static void main(String[] args) {
        MyClass instance = new MyClass();
        ClassLoader clNonCompliant = instance.getClassLoaderNonCompliant();
        ClassLoader clCompliant = instance.getClassLoaderCompliant();

        System.out.println("Non-compliant ClassLoader: " + clNonCompliant);
        System.out.println("Compliant ClassLoader: " + clCompliant);

        // In a simple standalone application, these might often be the same.
        // The issue S3032 highlights is specific to JEE environments where
        // the class loading hierarchy can be more complex.
        if (clNonCompliant != null && clCompliant != null) {
            System.out.println("Are they the same in this context? " + (clNonCompliant == clCompliant));
        }
    }
}
