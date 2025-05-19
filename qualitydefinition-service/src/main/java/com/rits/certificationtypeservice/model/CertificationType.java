package com.rits.certificationtypeservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_CERTIFICATIONTYPE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CertificationType {
    private String site;
    @Id
    private String handle;
    private String certificationType;
    private String description;
    private String filterCertificationId;
    private String filterDescription;
    private List<Certification> certificationList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
