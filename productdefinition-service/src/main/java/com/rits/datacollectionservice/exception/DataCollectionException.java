package com.rits.datacollectionservice.exception;

public class DataCollectionException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public DataCollectionException(int code, Object... args) {
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
