package com.rits.certificationservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationResponseList {
    private List<CertificationResponse> certificationList;
}
