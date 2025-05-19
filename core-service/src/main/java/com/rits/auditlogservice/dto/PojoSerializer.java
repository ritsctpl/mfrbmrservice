package com.rits.auditlogservice.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class PojoSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            // Handle the exception, or return null as needed.
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        // This method can be left empty
    }
}