package com.rits.shoporderservice.exception;

public class ShopOrderException extends RuntimeException {
    private int code;
    private Object[] args;

    public ShopOrderException(int code, Object... args) {
        super();
        this.code = code;
        this.args = args;
    }

    public int getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String getMessage() {
        return String.valueOf(code);
    }
}
