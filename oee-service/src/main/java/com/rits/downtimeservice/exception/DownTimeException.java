package com.rits.downtimeservice.exception;

public class DownTimeException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public DownTimeException(int code, Object... args) {
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
