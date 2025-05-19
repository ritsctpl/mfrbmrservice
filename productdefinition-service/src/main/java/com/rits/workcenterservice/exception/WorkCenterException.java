package com.rits.workcenterservice.exception;

import java.text.MessageFormat;

public class WorkCenterException extends RuntimeException {
    private int code;
    private Object[] args;

    public WorkCenterException(int code, Object... args) {
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
