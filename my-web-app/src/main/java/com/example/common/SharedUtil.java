package com.example.common;

public class SharedUtil {
    public void printClassLoaderInfo(String context) {
        System.out.println(context + ": Util's ClassLoader: " + this.getClass().getClassLoader());
        System.out.println(context + ": Util's Class Name: " + this.getClass().getName());
        System.out.println(context + ": Thread Context ClassLoader: " + Thread.currentThread().getContextClassLoader());
    }
}
