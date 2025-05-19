package com.rits.usercertificateassignmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCertificateAssignmentMessageModel {
    private UserCertificateAssignment response;
    private MessageDetails message_details;
}
