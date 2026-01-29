package com.embrace.cup.api;

import java.util.Map;

import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseJson;
import com.embrace.cup.zoo.ResponseWeb;

public class TaskFind implements Handler {
    @Override
    public  ResponseWeb handle(Map<String, Object> params){
        return ResponseJson.ok(params);
    }
}
