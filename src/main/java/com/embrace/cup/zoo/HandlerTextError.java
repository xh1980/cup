package com.embrace.cup.zoo;


public class HandlerTextError extends RuntimeException {
    private Integer status;
    private String code;
    
    public Integer getStatus() {
        return status;
    }
    public String getCode() {
        return code;
    }

    public HandlerTextError(Integer status, String code, String msg) {
        super(msg);
        this.status = status;
        this.code = code;
    }

    public HandlerTextError(String code, String msg) {
        super(msg);
        this.status = 200;
        this.code = code;
    }
    
}
