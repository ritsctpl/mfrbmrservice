package com.rits.pco.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMessageRequest {
    private String topic;
    private String key;
    private JsonNode message;  // ✅ Handles JSON object natively
}
