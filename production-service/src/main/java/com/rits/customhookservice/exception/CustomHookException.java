package com.rits.customhookservice.exception;

public class CustomHookException extends RuntimeException {
    private int code;
    private Object[] args;

    public CustomHookException(int code, Object... args) {
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
