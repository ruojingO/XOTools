package com.example;

public class MyCustomResource {
    private String name;

    public MyCustomResource(String name) {
        this.name = name;
        System.out.println("Resource '" + name + "' opened.");
    }

    public static MyCustomResource createResource(String name) {
        return new MyCustomResource(name);
    }

    public void initialize() {
        System.out.println("Resource '" + name + "' initialized.");
    }

    public void close() {
        System.out.println("Resource '" + name + "' closed.");
    }
}
