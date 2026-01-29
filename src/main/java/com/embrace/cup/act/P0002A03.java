package com.embrace.cup.act;

import java.util.Map;

import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0002A03 implements Handler {
    
    @Override
    public ResponseWeb handle(Map<String, Object> params) {
        
        throw new RuntimeException("defatut error page test");
    }
}
