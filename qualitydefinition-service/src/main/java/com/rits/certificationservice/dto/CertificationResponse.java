package com.rits.certificationservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationResponse {
    private String certification;
    private String description;
    private String status;
}
