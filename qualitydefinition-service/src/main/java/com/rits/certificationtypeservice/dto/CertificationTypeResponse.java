package com.rits.certificationtypeservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationTypeResponse {
    private String certificationType;
    private String description;
}
