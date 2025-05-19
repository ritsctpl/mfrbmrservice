package com.rits.logbuyoffservice.exception;

public class LogBuyOffException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public LogBuyOffException(int code, Object... args) {
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
