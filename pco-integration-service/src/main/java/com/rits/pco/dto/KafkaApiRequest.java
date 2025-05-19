package com.rits.pco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class KafkaApiRequest {
    private String correlationId;
    private String pcoId;
    private String agentId;
    private String apiUrl;
    private Map<String, Object> request;
    private String method;
}
