package com.rits.batchnorecipeheaderservice.exception;

public class BatchNoRecipeHeaderException extends RuntimeException {
    private int code;
    private Object[] args;

    public BatchNoRecipeHeaderException(int code, Object... args) {
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
