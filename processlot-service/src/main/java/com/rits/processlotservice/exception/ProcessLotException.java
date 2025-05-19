package com.rits.processlotservice.exception;

public class ProcessLotException extends RuntimeException {
    private int code;
    private Object[] args;

    public ProcessLotException(int code, Object... args) {
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
