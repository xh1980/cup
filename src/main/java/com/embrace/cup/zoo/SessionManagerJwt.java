package com.embrace.cup.zoo;

import java.util.Map;

public class SessionManagerJwt implements SessionManager {
    private static final String LOGTAG = SessionManagerJwt.class.getSimpleName();
    
    @Override
    public Map<String, Object> getSessionData(String sessionId) {
        Log.info(LOGTAG, sessionId);
        return UtilJwt.parseToken(sessionId, ConfigHolder.SESSION_SECRET);
    }

    @Override
    public String saveSessionData(Map<String, Object> data, int ageSeconds, String sessionId) {
        String sid = UtilJwt.generateToken(data, ageSeconds*1000, ConfigHolder.SESSION_SECRET);
        return sid;
    }
    
    @Override
    public void clearSession(String sessionId) {
        
    }
}
