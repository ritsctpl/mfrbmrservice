package com.rits.certificationtypeservice.dto;

import com.rits.certificationtypeservice.model.Certification;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificateResponse {
    private List<Certification> certificationList;
}
