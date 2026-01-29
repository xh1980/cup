package com.embrace.cup.zoo;

public class ErrorJson extends RuntimeException {
    public int status; 
    public String code;

    public ErrorJson(int status, String code, String msg) {
        super(msg);
        this.status = status;
        this.code = code;
    }
}
