package com.embrace.cup.auth;

import java.util.List;
import java.util.Map;

import com.embrace.cup.zoo.AuthInfo;
import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.HandlerTextError;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.Log;
import com.embrace.cup.zoo.ResponseWeb;

public class Login implements Handler {
    private static final String LOGTAG = Login.class.getSimpleName();
    private static final String E01_CODE = Login.class.getSimpleName() + "-E01";
    private static final String E01_MSG  = "username or password error";

    @Override
    public ResponseWeb handle(Context ctx) {
        Log.info(LOGTAG, "----------------------------------------");
        Boolean authenticated = ctx.getAuthInfo().getAuthenticated();

        if (authenticated == true) return ctx.renderRedirect("/");
        
        Map<String, Object> params = ctx.getParameters();

        String username = (String) params.get("username");
        String password = (String) params.get("password");
        if (username != null && password != null 
            && !username.isEmpty() && !password.isEmpty()) {
            
            AuthInfo.login(
                true, 
                username, username, username, 
                List.of("R0001", "R0002"),
                List.of("P0001A01", "P0001A02", "P0001A03", "P0002A01"),
                "E00001"
            );
            
            return ctx.renderRedirect("/");
        } else {
            throw new HandlerTextError(E01_CODE, E01_MSG);
        }
        
    }
    
    @Override
    public List<String> allowedMethods() {
        return List.of("POST", "GET");
    }
    @Override
    public Boolean LoginRequired() {
       return false;
    }
    @Override
    public Boolean PermissionRequired() {
       return false;
    }
}
