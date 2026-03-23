package com.embrace.cup.zoo;

import java.util.List;

public class ConfigHolder {
    private static String LOGTAG = ConfigHolder.class.getSimpleName();

    public static final String APP_PORT = ConfigReader.getStringValue("app.port");
    public static final String APP_PACKAGE = ConfigReader.getStringValue("app.package");
    public static final List<String> APP_HANDLER_PACKAGES = ConfigReader.getStringArray("app.handler-packages");
    
    public static final String JOB_PACKAGE = ConfigReader.getStringValue("job.package");

    public static final String SESSION_COOKIE_NAME = ConfigReader.getStringValue("session.cookie-name");
    public static final Integer SESSION_AGE_MINUTS = ConfigReader.getIntegerValue("session.age-minuts");
    public static final Integer SESSION_AGE = 60 * SESSION_AGE_MINUTS;
    public static final String SESSION_MANAGER_CLASS = ConfigReader.getStringValue("session.manager");
    public static final String SESSION_SECRET = ConfigReader.getStringValue("session.secret");
    public static final SessionManager SESSION_MANAGER;
    
    public static final String LOG_LEVEL = ConfigReader.getStringValue("log.level");

    public static final String DB_URL = ConfigReader.getStringValue("db.url");
    public static final String DB_USERNAME = ConfigReader.getStringValue("db.username");
    public static final String DB_PASSWORD = ConfigReader.getStringValue("db.password");
    public static final String DB_DRIVER = ConfigReader.getStringValue("db.driver");
    public static final Integer DB_POOL_SIZE = ConfigReader.getIntegerValue("db.maxPoolSize");


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
