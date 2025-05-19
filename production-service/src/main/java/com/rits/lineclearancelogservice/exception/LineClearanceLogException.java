package com.rits.lineclearancelogservice.exception;

public class LineClearanceLogException extends RuntimeException {
    private int code;
    private Object[] args;

    public LineClearanceLogException(int code, Object... args) {
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
