package com.rits.pco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRequest {
    private String correlationId;
    private String pcoId;
    private String agentId;
    private String apiUrl;
    private Map<String, Object> request;  // ✅ Accepts JSON object
    private String method;
}