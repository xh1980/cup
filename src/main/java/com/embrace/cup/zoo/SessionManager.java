package com.embrace.cup.zoo;

import java.util.Map;

public interface SessionManager {
    
    public Map<String, Object> getSessionData(String sessionId);
    public String saveSessionData(Map<String, Object> data, int ageSeconds, String sessionId);
    public void clearSession(String sessionId);

}