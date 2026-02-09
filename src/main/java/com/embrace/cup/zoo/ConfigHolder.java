package com.embrace.cup.zoo;

import java.util.Arrays;
import java.util.List;

public class ConfigHolder {
    private static String LOGTAG = ConfigHolder.class.getSimpleName();

    public static final String APP_PORT = Config.get("app.port");
    public static final String APP_PACKAGE = Config.get("app.package");

    public static final String SESSION_COOKIE_NAME = Config.get("session.cookie-name");
    public static final String SESSION_AGE_CONFIG = Config.get("session.age-minuts");
    public static final int SESSION_AGE = 60 * Integer.parseInt(SESSION_AGE_CONFIG);
    public static final String SESSION_MANAGER_CLASS = Config.get("session.manager");
    public static final String SESSION_SECRET = Config.get("session.secret");
    public static final SessionManager SESSION_MANAGER;

    // public static final String HANDLER_PACKAGES = Config.get("handler.packages");
    // public static final List<String> HANDLER_PACKAGES_LIST = Arrays.stream(
    //                                                             HANDLER_PACKAGES.split(","))
    //                                                             .map(String::trim)
    //                                                             .filter(e -> !e.isEmpty())
    //                                                             .toList();
    public static final String JOB_PACKAGE = Config.get("job.package");
    public static final String AUTH_EXCLUDE = Config.get("auth.exclude");
    public static final List<String> AUTH_EXCLUDE_LIST = Arrays.stream(
                                                                AUTH_EXCLUDE.split(","))
                                                                .map(String::trim)
                                                                .filter(e -> !e.isEmpty())
                                                                .toList();
    public static final String AUTH_DEFAULT_PERMISSIONS = Config.get("auth.default-permissions");
    public static final List<String> AUTH_DEFAULT_PERM_LIST = Arrays.stream(
                                                            AUTH_DEFAULT_PERMISSIONS.split(","))
                                                            .map(String::trim)
                                                            .filter(e -> !e.isEmpty())
                                                            .toList();

    public static final String DB_URL = Config.get("db.url");
    public static final String DB_USERNAME = Config.get("db.username");
    public static final String DB_PASSWORD = Config.get("db.password");
    public static final String DB_DRIVER = Config.get("db.driver");
    public static final Integer DB_POOL_SIZE = Integer.parseInt(Config.get("db.maxPoolSize"));


    static {
        try {
            Class<?> clazz = Class.forName(
                ConfigHolder.APP_PACKAGE + ".zoo." + ConfigHolder.SESSION_MANAGER_CLASS
            );
            Object obj = clazz.getDeclaredConstructor().newInstance();
            SESSION_MANAGER = (SessionManager) obj;
        } catch (Exception e) {
            Log.error(LOGTAG, "session manager class error");
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }
    
}
