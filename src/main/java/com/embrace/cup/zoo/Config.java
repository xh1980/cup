package com.embrace.cup.zoo;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    private static final Map<String, Object> yaml;

    static {
        try (InputStream in = Config.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {

            if (in == null) throw new RuntimeException("application.yml not found");

            yaml = new Yaml().load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String get(String path) {
        Object val = getValue(path);
        if (val == null) return null;
        return resolveEnv(val.toString());
    }

    private static Object getValue(String path) {
        String[] keys = path.split("\\.");
        Object cur = yaml;

        for (String k : keys) {
            if (!(cur instanceof Map<?, ?> map)) return null;
            cur = map.get(k);
        }
        return cur;
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
}
