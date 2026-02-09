package com.embrace.cup.act;

import java.util.Map;

import com.embrace.cup.zoo.Context;
import com.embrace.cup.zoo.Handler;
import com.embrace.cup.zoo.ResponseHtml;

public class P0001A03 implements Handler {

    @Override
    public ResponseHtml handle(Context ctx) {

        return ResponseHtml.fromFile("html/index.html");
    }
}

