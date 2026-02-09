package com.embrace.cup.act;

import java.util.Map;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseJson;
import com.embrace.cup.zoo.ResponseWeb;
import com.embrace.cup.zoo.AuthInfo;



public class Logout implements Handler {

    @Override
    public ResponseWeb handle(Context ctx) {
        
        AuthInfo.logout();
        return ResponseJson.ok(Map.of("message", "logout success"));
    }
    
}
