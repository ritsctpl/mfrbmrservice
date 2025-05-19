package com.rits.assemblyservice.exception;

public class AssemblyException extends RuntimeException {
    private int code;
    private Object[] args;

    public AssemblyException(int code, Object... args) {
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
