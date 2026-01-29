package com.embrace.cup.zoo;

import java.util.List;
import java.util.Map;

public class SessionManager {
    
    public Map<String, Object> getSessionData(String sessionId) {
        Log.info("RequestContextManager", sessionId);
        // TODO  get session data by sessId
        return Map.of(
            "userName", "user-test",
            "userEmail", "user-test@test.com",
            "roles", List.of("R001","R002"),
            "permissions", List.of("P001","P002"),
            "employeeNo", "E000001"
        );
    }

    public String saveSessionData(Map<String, Object> data, int age, String sessionId) {
        String sId = "aaa";
        return sId;
    }
}
