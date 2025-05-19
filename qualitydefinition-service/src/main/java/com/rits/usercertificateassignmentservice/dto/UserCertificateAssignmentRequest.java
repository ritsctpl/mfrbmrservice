package com.rits.usercertificateassignmentservice.dto;

import com.rits.usercertificateassignmentservice.model.CertificationDetails;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserCertificateAssignmentRequest {
    private String site;
    private String userId;
    private String user;
    private String userGroup;
    private List<CertificationDetails> certificationDetailsList;
    private String handle;

}
