package com.rits.performance.exception;

public class PerformanceException extends RuntimeException{
    private int code;
    private Object[] args;

    public PerformanceException(int code, Object... args) {
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
