package com.rits.mfrservice.model;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FooterDetails {
    private String role;
    private String department;
    private String authorizedBy;
    private String dateOfAuthorization;
    private String approvalSignature;
}
