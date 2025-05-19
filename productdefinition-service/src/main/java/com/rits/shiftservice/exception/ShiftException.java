package com.rits.shiftservice.exception;

public class ShiftException extends RuntimeException{
    private int code;
    private Object[] args;

    public ShiftException(int code,  Object... args) {
        super();
        this.code = code;
        this.args = args;
    }

    public ShiftException(String s) {
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
