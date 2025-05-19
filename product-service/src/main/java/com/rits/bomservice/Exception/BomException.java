package com.rits.bomservice.Exception;

public class BomException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public BomException(int code, Object... args) {
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
