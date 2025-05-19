package com.rits.pco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandshakeRequest {
    private String correlationId;
    private String pcoId;
    private String username;
}