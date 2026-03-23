package com.embrace.cup.act;

import java.util.List;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseRedirect;

public class P0001A02 implements Handler {

    @Override
    public ResponseRedirect handle(Context ctx) {

         return ctx.renderRedirect("/act/P0001A01");
    }

    @Override
    public List<String> allowedMethods() {
        return List.of("GET");
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
