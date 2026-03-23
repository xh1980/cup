package com.embrace.cup.act;

import java.util.List;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0001A01 implements Handler {

    @Override
    public ResponseWeb handle(Context ctx) {
       return ctx.renderJson(ctx.getParameters());
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
