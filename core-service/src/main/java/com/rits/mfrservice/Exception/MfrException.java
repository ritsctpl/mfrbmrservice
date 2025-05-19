package com.rits.mfrservice.Exception;

public class MfrException extends RuntimeException {
    private int code;
    private Object[] args;

    public MfrException(int code, Object... args) {
        super();
        this.code = code;
        this.args = args;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.valueOf(code);
    }

    public Object[] getArgs() {
        return args;
    }
}