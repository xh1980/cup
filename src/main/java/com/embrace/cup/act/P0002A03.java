package com.embrace.cup.act;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseWeb;

public class P0002A03 implements Handler {
    
    @Override
    public ResponseWeb handle(Context ctx) {
        
        throw new RuntimeException("defatut error page test");
    }
}
