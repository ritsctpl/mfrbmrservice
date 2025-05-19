package com.rits.usercertificateassignmentservice.model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertificationDetails {
    private  String certification;
    private String description;
    private String status;
    private String dateExpires;
    private String dateCertified;
    private String extension;
    private String comments;
    private String currentExtension;
    private String maxExtension;
    private String extensionDuration;
}
