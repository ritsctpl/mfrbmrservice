package com.rits.usercertificateassignmentservice.controller;

import com.rits.usercertificateassignmentservice.dto.UserCertificateAssignmentRequest;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignment;
import com.rits.usercertificateassignmentservice.model.UserCertificateAssignmentMessageModel;
import com.rits.usercertificateassignmentservice.service.UserCertificateAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("app/v1/usercertificateassignment-service")
public class UserCertificateAssignmentController {

    private final UserCertificateAssignmentService userCertificateAssignmentService;

    @Autowired
    public UserCertificateAssignmentController(UserCertificateAssignmentService userCertificateAssignmentService) {
        this.userCertificateAssignmentService = userCertificateAssignmentService;
    }

    @PostMapping("save")
    public UserCertificateAssignmentMessageModel saveUserCertificateAssignment(@RequestBody UserCertificateAssignmentRequest userCertificateAssignment) throws Exception {
        return userCertificateAssignmentService.save(userCertificateAssignment);
    }
    @PostMapping("update")
    public UserCertificateAssignmentMessageModel updateUserCertificateAssignment(@RequestBody UserCertificateAssignmentRequest userCertificateAssignment) throws Exception {
        return userCertificateAssignmentService.update(userCertificateAssignment);
    }
    @PostMapping("delete")
    public UserCertificateAssignmentMessageModel deleteUserCertificateAssignment(@RequestBody UserCertificateAssignmentRequest userCertificateAssignment) {
        return userCertificateAssignmentService.delete(userCertificateAssignment.getSite(),  userCertificateAssignment.getUser(), userCertificateAssignment.getUserGroup(), userCertificateAssignment.getUserId());
    }
    @PostMapping("retrieveByUser")
    public UserCertificateAssignment retrieveUserCertificateAssignment(@RequestBody UserCertificateAssignment userCertificateAssignment) {
        String site = userCertificateAssignment.getSite();
        String user = userCertificateAssignment.getUser();
        return userCertificateAssignmentService.retrieveByUser(site, user);
    }
    @PostMapping("retrieveByUserGroup")
    public UserCertificateAssignment retrieveByUserGroupUserCertificateAssignment(@RequestBody UserCertificateAssignment userCertificateAssignment) {
        String site = userCertificateAssignment.getSite();
        String userGroup = userCertificateAssignment.getUserGroup();
        return userCertificateAssignmentService.retrieveByUserGroup(site, userGroup);
    }

}
