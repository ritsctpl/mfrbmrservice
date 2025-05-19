package com.rits.resourceservice.exception;

public class ResourceException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public ResourceException(int code, Object... args) {
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
