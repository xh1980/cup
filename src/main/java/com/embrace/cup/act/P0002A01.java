package com.embrace.cup.act;



import java.util.Map;

import com.embrace.cup.zoo.ErrorJsonRequest;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0002A01 implements Handler {
    private static final String ERR_E01_CODE = P0002A01.class.getSimpleName() + "-E01";
    private static final String ERR_E01_MSG  = "エラー１";

    @Override
    public ResponseWeb handle(Map<String, Object> params) {
        throw new ErrorJsonRequest(ERR_E01_CODE, ERR_E01_MSG);
    }

}
