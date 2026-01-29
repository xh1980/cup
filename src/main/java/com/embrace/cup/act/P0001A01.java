package com.embrace.cup.act;

import java.util.Map;

import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseJson;
import com.embrace.cup.zoo.ResponseWeb;

public class P0001A01 implements Handler {

    @Override
    public ResponseWeb handle(Map<String, Object> params) {

        return ResponseJson.ok(params);
    }

}
