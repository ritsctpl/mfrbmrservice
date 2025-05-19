package com.rits.site.exception;

public class SiteException extends RuntimeException {
    private int code;
    private Object[] args;

    public SiteException(int code, Object... args) {
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
