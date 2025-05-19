package com.rits.oeeservice.util;

import com.rits.oeeservice.dto.ParameterMetaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;

public class ParameterConverter {

    private static Object convertValue(String type, Object value) {
        if (value == null) return null;
        try {
            switch (type.toUpperCase()) {
                case "DATE":
                    return java.sql.Date.valueOf(value.toString());
                case "INTEGER":
                    return Integer.parseInt(value.toString());
                case "DOUBLE":
                    return Double.parseDouble(value.toString());
                case "ARRAY":
                    // If the input is a JSON string representing an array, parse it using Jackson.
                    // If it is already a collection, return it directly.
                    if (value instanceof Collection) {
                        return value;
                    } else {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        return mapper.readValue(value.toString(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Object>>() {});
                    }
                case "STRING":
                default:
                    return value.toString();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid value for type " + type + ": " + value, ex);
        }
    }


    public static List<ParameterMetaDto> convertJsonToParameterList(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(json, ParameterMetaDto[].class));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to parse input parameters JSON", ex);
        }
    }

    public static Map<String, Object> validateAndConvertParameters(List<ParameterMetaDto> paramMetas, Map<String, Object> input) {
        Map<String, Object> processedParams = new HashMap<>();
        for (ParameterMetaDto meta : paramMetas) {
            Object rawValue = input.get(meta.getName());
            if (meta.isRequired() && rawValue == null) {
                throw new IllegalArgumentException("Missing required parameter: " + meta.getName());
            }
            if (rawValue != null) {
                processedParams.put(meta.getName(), convertValue(meta.getType(), rawValue));
            }
        }
        return processedParams;
    }
}
