package com.embrace.cup.zoo;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

@Data
public class Context {
    private Map<String, Object> parameters;
    private AuthInfo authInfo;

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private String packageName;
    private String className;
    private String classFullName;
    private String uri;
    private String sessionId;
    

}
