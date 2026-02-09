package com.embrace.cup.zoo;

public class ErrorJson extends RuntimeException {
    public String code;

    public ErrorJson(String code, String msg) {
        super(msg);
        this.code = code;
    }
}
