package com.embrace.cup.auth;

import java.util.List;
import java.util.Map;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;
import com.embrace.cup.zoo.AuthInfo;



public class Logout implements Handler {

    @Override
    public ResponseWeb handle(Context ctx) {
        
        AuthInfo.logout();
        return ctx.renderJson(Map.of("message", "logout success"));
    }
    @Override
    public List<String> allowedMethods() {
        return List.of("POST");
    }
    @Override
    public Boolean LoginRequired() {
       return true;
    }
    @Override
    public Boolean PermissionRequired() {
       return false;
    }
}
