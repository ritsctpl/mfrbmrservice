package com.rits.oeeservice.util;

import java.util.Map;

public class OutputMapper {

    public static Object mapOutput(Map<String, Object> procedureOutput) {
        if (procedureOutput.containsKey("result")) {
            return procedureOutput.get("result");
        }
        if (procedureOutput.containsKey("RESULT_SET")) {
            return procedureOutput.get("RESULT_SET");
        }
        return procedureOutput;
    }
}
