package com.embrace.cup.zoo;

import java.util.Map;

public class RequestContext {
    
    private static final ThreadLocal<Map<String, Object>> CTX = new ThreadLocal<>();

    protected static void set(Map<String, Object> data) {
        CTX.set(data);
    }

    protected static Map<String, Object> get() {
        return CTX.get();
    }

    protected static void clear() {
        CTX.remove();
    }

}
