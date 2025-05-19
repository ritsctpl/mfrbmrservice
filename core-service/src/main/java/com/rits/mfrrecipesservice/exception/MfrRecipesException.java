package com.rits.mfrrecipesservice.exception;

public class MfrRecipesException extends RuntimeException {
    private int code;
    private Object[] args;

    public MfrRecipesException(int code, Object... args) {
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
