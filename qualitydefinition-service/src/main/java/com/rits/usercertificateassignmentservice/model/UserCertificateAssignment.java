package com.rits.usercertificateassignmentservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_USER_CERTIFICATE_ASSIGNMENT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserCertificateAssignment {
    @Id
    private String handle;
    private String site;
    private String user;
    private String userGroup;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
    private List<CertificationDetails> certificationDetailsList;
}
