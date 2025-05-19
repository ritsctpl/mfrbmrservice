package com.rits.availability.exception;


public class AvailabilityException extends RuntimeException {
    private int code;
    private Object[] args;

    public AvailabilityException(int code, Object... args) {
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
