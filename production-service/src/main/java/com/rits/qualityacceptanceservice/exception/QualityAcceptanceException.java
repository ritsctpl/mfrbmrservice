package com.rits.qualityacceptanceservice.exception;

public class QualityAcceptanceException extends RuntimeException {
    private int code;
    private Object[] args;

    public QualityAcceptanceException(int code, Object... args) {
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
