package com.rits.workinstructionservice.exception;

import java.text.MessageFormat;

public class WorkInstructionException extends RuntimeException {
    private int code;
    private Object[] args;

    public WorkInstructionException(int code, Object... args) {
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
