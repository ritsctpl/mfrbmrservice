package com.rits.pco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PcoAgentResponse {
    private String correlationId;
    private String pcoId;
    private String agentId;
    private String fentaId; // MongoDB ID
    private String status; // Example: "registered"
}
