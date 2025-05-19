package com.rits.certificationtypeservice.dto;

import com.rits.certificationtypeservice.model.Certification;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationTypeRequest {
    private String site;
    private String certificationType;
    private String description;
    private String filterCertificationId;
    private String filterDescription;
    private List<Certification> certificationList;

}
