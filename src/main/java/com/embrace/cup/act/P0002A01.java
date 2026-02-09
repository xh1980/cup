package com.embrace.cup.act;


import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.ErrorJson;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0002A01 implements Handler {
    private static final String ERR_E01_CODE = P0002A01.class.getSimpleName() + "-E01";
    private static final String ERR_E01_MSG  = "エラー１";

    @Override
    public ResponseWeb handle(Context ctx) {
        throw new ErrorJson(ERR_E01_CODE, ERR_E01_MSG);
    }

}
