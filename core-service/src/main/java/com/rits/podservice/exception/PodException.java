package com.rits.podservice.exception;


public class PodException extends RuntimeException {
    private int code;
    private Object[] args;

    public PodException(int code, Object... args) {
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
