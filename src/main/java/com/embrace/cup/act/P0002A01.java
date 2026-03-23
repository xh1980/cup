package com.embrace.cup.act;


import java.util.List;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.HandlerTextError;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0002A01 implements Handler {
    private static final String ERR_E01_CODE = P0002A01.class.getSimpleName() + "-E01";
    private static final String ERR_E01_MSG  = "エラー１";

    @Override
    public ResponseWeb handle(Context ctx) {
        throw new HandlerTextError(ERR_E01_CODE, ERR_E01_MSG);
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
