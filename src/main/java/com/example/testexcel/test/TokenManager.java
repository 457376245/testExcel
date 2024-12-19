package com.example.testexcel.test;

public class TokenManager {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();

    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }

    public static String getToken() {
        return tokenThreadLocal.get();
    }

    public static void removeUser() {
        tokenThreadLocal.remove();
    }
}