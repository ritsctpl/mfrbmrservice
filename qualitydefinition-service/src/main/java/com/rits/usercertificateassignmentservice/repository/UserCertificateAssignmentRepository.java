package com.rits.usercertificateassignmentservice.repository;

import com.rits.usercertificateassignmentservice.model.UserCertificateAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCertificateAssignmentRepository extends MongoRepository<UserCertificateAssignment , String> {
    boolean existsByActiveAndSiteAndUserGroup(int i, String site, String userGroup);

    boolean existsByActiveAndSiteAndUser(int i, String site, String user);

    UserCertificateAssignment findByActiveAndSiteAndUserGroup(int i, String site, String userGroup);

    UserCertificateAssignment findByActiveAndSiteAndUser(int i, String site, String user);
}
