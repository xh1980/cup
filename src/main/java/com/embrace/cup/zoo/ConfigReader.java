package com.embrace.cup.zoo;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigReader {
    
    private static Map<String, Object> config;
    
    static {
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {
            
            Yaml yaml = new Yaml();
            config = yaml.load(in);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // 解析 ${ENV:default}
    private static final Pattern ENV_PATTERN =
            Pattern.compile("\\$\\{([^:}]+)(:([^}]*))?}");

    private static String resolveEnv(String val) {
        Matcher m = ENV_PATTERN.matcher(val);
        if (!m.matches()) return val;

        String env = m.group(1);
        String def = m.group(3);

        String real = System.getenv(env);
        return real != null ? real : def;
    }

    @SuppressWarnings("unchecked")
    private static Object getValue(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = config;
        
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.get(parts[i]);
            if (current == null) return null;
        }
        
        return current.get(parts[parts.length - 1]);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringArray(String key) {
        return (List<String>) getValue(key);
    }
    
    @SuppressWarnings("unchecked")
    public static List<Integer> getIntArray(String key) {
        return (List<Integer>) getValue(key);
    }

    public static Integer getIntegerValue(String key) {
        Object val = getValue(key);
        if (val == null) return null;
        String valString = resolveEnv(val.toString());
        return Integer.valueOf(valString);

    }

    public static String getStringValue(String key) {
        Object val = getValue(key);
        if (val == null) return null;
        String valString = resolveEnv(val.toString());
        return valString;
    }
    
}