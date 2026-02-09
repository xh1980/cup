package com.embrace.cup.act;

import java.util.Map;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseRedirect;

public class P0001A02 implements Handler {

    @Override
    public ResponseRedirect handle(Context ctx) {

         return ResponseRedirect.to("/act/P0001A01");
    }
}
