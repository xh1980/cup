package com.embrace.cup.act;

import java.util.List;
import java.util.Map;

import com.embrace.cup.zoo.AuthInfo;
import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.ErrorJson;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.Log;
import com.embrace.cup.zoo.ResponseRedirect;
import com.embrace.cup.zoo.ResponseWeb;

public class Login implements Handler {
    private static final String LOGTAG = Login.class.getSimpleName();
    private static final String E01_CODE = Login.class.getSimpleName() + "-E01";
    private static final String E01_MSG  = "username or password error";

    @Override
    public ResponseWeb handle(Context ctx) {
        Log.info(LOGTAG, "----------------------------------------");
        Boolean authenticated = ctx.getAuthInfo().getAuthenticated();

        if (authenticated == true) return ResponseRedirect.to("/");
        
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
            
            return ResponseRedirect.to("/");
        } else {
            throw new ErrorJson(E01_CODE, E01_MSG);
        }
        
    }
}
