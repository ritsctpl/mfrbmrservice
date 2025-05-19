package com.rits.batchnodoneservice.exception;

public class BatchNoDoneException extends RuntimeException {
    private int code;
    private Object[] args;

    public BatchNoDoneException(int code, Object... args) {
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
