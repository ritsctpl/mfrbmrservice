package com.rits.stepstatusservice.exception;

public class BatchStepStatusException extends RuntimeException {
    private int code;
    private Object[] args;

    public BatchStepStatusException(int code, Object... args) {
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
