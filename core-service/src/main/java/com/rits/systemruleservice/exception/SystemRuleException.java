
package com.rits.systemruleservice.exception;

public class SystemRuleException extends RuntimeException {
    public SystemRuleException(int i, String site) {
    }
    private int code;
    private Object[] args;

    public SystemRuleException(int code, Object... args) {
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

