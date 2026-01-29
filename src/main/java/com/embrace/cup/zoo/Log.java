package com.embrace.cup.zoo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Log {

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    // ===== 全局日志级别（来自环境变量）=====
    private static final Level GLOBAL_LEVEL = loadLevel();

    // ===== ThreadLocal：存 requestId =====
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    // ===== 对外 API =====

    public static void debug(String tag, String msg) {
        log(Level.DEBUG, tag, msg, null);
    }

    public static void info(String tag, String msg) {
        log(Level.INFO, tag, msg, null);
    }

    public static void warn(String tag, String msg) {
        log(Level.WARN, tag, msg, null);
    }

    public static void error(String tag, String msg) {
        log(Level.ERROR, tag, msg, null);
    }

    public static void error(String tag, String msg, Throwable t) {
        log(Level.ERROR, tag, msg, t);
    }

    public static void log(Level level, String tag, String msg, Throwable t) {
        if (level.ordinal() < GLOBAL_LEVEL.ordinal()) return;

        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        String thread = Thread.currentThread().getName();
        String rid = REQUEST_ID.get();

        String line = String.format(
                "%s [%s] [thread=%s] [rid=%s] [%s] %s",
                time,
                level,
                thread,
                rid == null ? "-" : rid,
                tag,
                msg
        );

        System.out.println(line);

        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    // ===== 给 Filter / Dispatcher 调用 =====
    public static void setRequestId() {
        REQUEST_ID.set(newRequestId());
    }
    public static void setRequestId(String rid) {
        REQUEST_ID.set(rid);
    }

    public static void clearRequestId() {
        REQUEST_ID.remove();
    }

    public static String newRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ===== 加载日志级别 =====
    private static Level loadLevel() {
        String v = System.getenv("LOG_LEVEL");
        if (v == null || v.isBlank()) return Level.INFO;

        try {
            return Level.valueOf(v.trim().toUpperCase());
        } catch (Exception e) {
            return Level.INFO;
        }
    }
}
