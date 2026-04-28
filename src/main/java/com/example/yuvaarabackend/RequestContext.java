package com.example.yuvaarabackend;

import org.springframework.stereotype.Component;

@Component
public class RequestContext {
    private static final ThreadLocal<Integer> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userEmailHolder = new ThreadLocal<>();

    public static void setUserId(Integer id) {
        userIdHolder.set(id);
    }

    public static Integer getUserId() {
        return userIdHolder.get();
    }

    public static void setUserEmail(String email) {
        userEmailHolder.set(email);
    }

    public static String getUserEmail() {
        return userEmailHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        userEmailHolder.remove();
    }
}
