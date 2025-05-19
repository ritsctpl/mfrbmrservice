package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FooterDetails {
    private String role;
    private String department;
    private String authorizedBy;
    private String dateOfAuthorization;
    private String approvalSignature;
}
