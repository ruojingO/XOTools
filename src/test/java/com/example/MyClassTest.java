package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyClassTest {

    @Test
    void testGetClassLoaderNonCompliant() {
        MyClass instance = new MyClass();
        ClassLoader cl = instance.getClassLoaderNonCompliant();
        assertNotNull(cl, "ClassLoader should not be null");
        System.out.println("NonCompliant ClassLoader from test: " + cl);
    }

    @Test
    void testGetClassLoaderCompliant() {
        MyClass instance = new MyClass();
        ClassLoader cl = instance.getClassLoaderCompliant();
        assertNotNull(cl, "ClassLoader should not be null");
        System.out.println("Compliant ClassLoader from test: " + cl);
    }

    @Test
    void compareLoadersInTest() {
        MyClass instance = new MyClass();
        ClassLoader clNonCompliant = instance.getClassLoaderNonCompliant();
        ClassLoader clCompliant = instance.getClassLoaderCompliant();
        assertNotNull(clNonCompliant);
        assertNotNull(clCompliant);

        // In a standard JUnit test environment (without specific JEE classloader setups),
        // these will likely be the same. The SonarQube rule is about correctness
        // in JEE containers, not necessarily about them being different in every context.
        System.out.println("NonCompliant in test: " + clNonCompliant);
        System.out.println("Compliant in test: " + clCompliant);
         //This assertion might be true or false depending on the test runner's classloader strategy
         //assertEquals(clNonCompliant, clCompliant, "ClassLoaders might be different in specific environments.");
    }
}
