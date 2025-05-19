package com.rits.itemservice.exception;

public class ItemException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public ItemException(int code, Object... args) {
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
