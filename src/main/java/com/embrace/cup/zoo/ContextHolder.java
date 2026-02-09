package com.embrace.cup.zoo;

public class ContextHolder {
    
    private static final ThreadLocal<Context> CTX = new ThreadLocal<>();

    protected static void set(Context ctx) {
        CTX.set(ctx);
    }

    public static Context get() {
        return CTX.get();
    }

    protected static void clear() {
        CTX.remove();
    }

}
