package com.rits.oeeservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OeeCalculationResponse {
    private List<OeeResponse> oeeResponses;
    private List<ShiftMessage> messages;
}
