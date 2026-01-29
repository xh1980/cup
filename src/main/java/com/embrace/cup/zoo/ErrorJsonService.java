package com.embrace.cup.zoo;

public class ErrorJsonService extends ErrorJson {

    public ErrorJsonService(String code, String msg) {
        super(500, code, msg);
    }
}
