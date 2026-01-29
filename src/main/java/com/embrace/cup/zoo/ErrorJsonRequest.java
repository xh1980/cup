package com.embrace.cup.zoo;

public class ErrorJsonRequest extends ErrorJson {

    public ErrorJsonRequest(String code, String msg) {
        super(400, code, msg);
    }
}
