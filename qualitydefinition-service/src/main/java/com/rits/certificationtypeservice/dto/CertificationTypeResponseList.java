package com.rits.certificationtypeservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationTypeResponseList {
    private List<CertificationTypeResponse> certificationTypeList;
}
