package com.rits.usercertificateassignmentservice.service;

import com.rits.usercertificateassignmentservice.dto.UserCertificateAssignmentRequest;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignment;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignmentMessageModel;

public interface UserCertificateAssignmentService {
    UserCertificateAssignmentMessageModel save(UserCertificateAssignmentRequest userCertificateAssignmentRequest) throws Exception;
    UserCertificateAssignmentMessageModel update(UserCertificateAssignmentRequest userCertificateAssignmentRequest) throws Exception;
    UserCertificateAssignmentMessageModel delete(String site, String user,String userGroup,String userId);

    UserCertificateAssignment retrieveByUser(String site, String user);

    UserCertificateAssignment retrieveByUserGroup(String site, String userGroup);

}
