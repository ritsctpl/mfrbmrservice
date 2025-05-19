package com.rits.toolnumberservice.exception;

import java.text.MessageFormat;

public class ToolNumberException extends RuntimeException {
    private int code;
    private Object[] args;

    public ToolNumberException(int code, Object... args) {
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