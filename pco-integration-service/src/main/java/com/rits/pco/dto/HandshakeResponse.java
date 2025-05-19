package com.rits.pco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandshakeResponse {
    private String correlationId;
    private String pcoId;
    private String status; // Example: "registered"
}
