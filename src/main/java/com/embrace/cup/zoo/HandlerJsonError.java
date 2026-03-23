package com.embrace.cup.zoo;

public class HandlerJsonError extends RuntimeException{
    private Integer status;
    private String code;
    
    public Integer getStatus() {
        return status;
    }
    public String getCode() {
        return code;
    }

    public HandlerJsonError(Integer status, String code, String msg) {
        super(msg);
        this.status = status;
        this.code = code;
    }

    public HandlerJsonError(String code, String msg) {
        super(msg);
        this.status = 200;
        this.code = code;
    }
}
