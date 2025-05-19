package com.rits.usergroupservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.usergroupservice.dto.*;
import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.usergroupservice.model.*;
import com.rits.usergroupservice.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/usergroup-service")
public class UserGroupController {
    private final UserGroupService userGroupService;
    private final ApplicationEventPublisher eventPublisher;


    @PostMapping("create")
    public ResponseEntity<UserGroupMessageModel> createUserGroup(@RequestBody UserGroupRequest userGroupRequest) throws Exception {
        UserGroupMessageModel createUserGroup;

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            createUserGroup = userGroupService.createUserGroup(userGroupRequest);
            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(userGroupRequest.getSite())
                    .action_code("USR-GROUP-CREATED")
                    .action_detail("User Group Created")
                    .action_detail_handle("ActionDetailBO:" + userGroupRequest.getSite() + "," + "USR-GROUP-CREATED" + userGroupRequest.getUserId() + ":" + "com.rits.usergroupservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(userGroupRequest.getUserId())
                    .txnId("USR-GROUP-CREATED" + String.valueOf(LocalDateTime.now()) + userGroupRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Create")
                    .topic("audit-log")
                    .build();

            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(UserGroupMessageModel.builder().message_details(createUserGroup.getMessage_details()).response(createUserGroup.getResponse()).build());
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("update")
    public ResponseEntity<UserGroupMessageModel> updateUserGroup(@RequestBody UserGroupRequest userGroupRequest) throws Exception {
        UserGroupMessageModel updateUserGroup;

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            updateUserGroup = userGroupService.updateUserGroup(userGroupRequest);
            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(userGroupRequest.getSite())
                    .action_code("USR-GROUP-UPDATED")
                    .action_detail("User Group Updated")
                    .action_detail_handle("ActionDetailBO:" + userGroupRequest.getSite() + "," + "USR-GROUP-UPDATED" + userGroupRequest.getUserId() + ":" + "com.rits.usergroupservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(userGroupRequest.getUserId())
                    .txnId("USR-GROUP-UPDATED" + String.valueOf(LocalDateTime.now()) + userGroupRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Update")
                    .topic("audit-log")
                    .build();

            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(UserGroupMessageModel.builder().message_details(updateUserGroup.getMessage_details()).response(updateUserGroup.getResponse()).build());
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public ResponseEntity<UserGroupMessageModel> deleteUserGroup(@RequestBody UserGroupRequest userGroupRequest) throws Exception {
        UserGroupMessageModel deleteResponse;

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            deleteResponse = userGroupService.deleteUserGroup(userGroupRequest.getSite(), userGroupRequest.getUserGroup(), userGroupRequest.getUserId());
            AuditLogRequest auditlog = AuditLogRequest.builder()
                    .site(userGroupRequest.getSite())
                    .action_code("USR-GROUP-DELETED")
                    .action_detail("User Group Deleted")
                    .action_detail_handle("ActionDetailBO:" + userGroupRequest.getSite() + "," + "USR-GROUP-DELETED" + userGroupRequest.getUserId() + ":" + "com.rits.usergroupservice.controller")
                    .activity("From Service")
                    .date_time(String.valueOf(LocalDateTime.now()))
                    .userId(userGroupRequest.getUserId())
                    .txnId("USR-GROUP-DELETED" + String.valueOf(LocalDateTime.now()) + userGroupRequest.getUserId())
                    .created_date_time(String.valueOf(LocalDateTime.now()))
                    .category("Delete")
                    .topic("audit-log")
                    .build();

            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(deleteResponse);
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("retrieve")
    public ResponseEntity<UserGroup> retrieveRouting(@RequestBody UserGroupRequest routingRequest) throws Exception {

        if (StringUtils.isEmpty(routingRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.retrieveUserGroup(routingRequest.getSite(), routingRequest.getUserGroup()));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("retrieveAll")
    public ResponseEntity<UserGroupResponseList> getUserGroupList(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.getAllUserGroup(userGroupRequest.getSite(), userGroupRequest.getUserGroup()));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("retrieveTop50")
    public ResponseEntity<UserGroupResponseList> getUserGroupListByCreationDate(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.getAllUserGroupByCreationDate(userGroupRequest.getSite()));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getAvailableUser")
    public ResponseEntity<List<User>> getAvailableUser(@RequestBody UserGroupRequest userGroupRequest) {
        List<User> getAvailableUser;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                getAvailableUser = userGroupService.getAvailableUser(userGroupRequest.getSite(), userGroupRequest.getUserGroup());
                return ResponseEntity.ok(getAvailableUser);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("getAvailableActivity")
    public ResponseEntity<List<Activity>> getAvailableActivity(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.getAvailableActivity(userGroupRequest.getSite(), userGroupRequest.getUserGroup()));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getAvailableActivityGroup")
    public ResponseEntity<List<ActivityGroup>> getAvailableActivityGroup(@RequestBody UserGroupRequest userGroupRequest) {
        List<ActivityGroup> getAvailableActivityGroup;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                getAvailableActivityGroup = userGroupService.getAvailableActivityGroup(userGroupRequest.getSite(), userGroupRequest.getUserGroup());
                return ResponseEntity.ok(getAvailableActivityGroup);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("assignUser")
    public Boolean assignUser(@RequestBody UserGroupRequest userGroupRequest) {
        Boolean assignUser;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                assignUser = userGroupService.assignUser(userGroupRequest.getSite(), userGroupRequest.getUserGroup(), userGroupRequest.getUsers());
                return (assignUser);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("removeUser")
    public Boolean removeUser(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return userGroupService.removeUser(userGroupRequest.getSite(), userGroupRequest.getUserGroup(), userGroupRequest.getUsers());
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("assignActivity")
    public ResponseEntity<List<Activity>> assignActivity(@RequestBody UserGroupRequest userGroupRequest) {
        List<Activity> assignActivity;
        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.assignActivity(userGroupRequest));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("removeActivity")
    public ResponseEntity<List<Activity>> removeActivity(@RequestBody UserGroupRequest userGroupRequest) {

        if (StringUtils.isEmpty(userGroupRequest.getSite()))
            throw new UserGroupException(1);

        try {
            return ResponseEntity.ok(userGroupService.removeActivity(userGroupRequest));
        } catch (UserGroupException userGroupException) {
            throw userGroupException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("assignActivityGroup")
    public ResponseEntity<List<ActivityGroup>> assignActivityGroup(@RequestBody UserGroupRequest userGroupRequest) {
        List<ActivityGroup> assignActivityGroup;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                assignActivityGroup = userGroupService.assignActivityGroup(userGroupRequest.getSite(), userGroupRequest.getUserGroup(), userGroupRequest.getPermissionForActivityGroup());
                return ResponseEntity.ok(assignActivityGroup);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("removeActivityGroup")
    public ResponseEntity<List<ActivityGroup>> removeActivityGroup(@RequestBody UserGroupRequest userGroupRequest) {
        List<ActivityGroup> removeActivityGroup;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                removeActivityGroup = userGroupService.removeActivityGroup(userGroupRequest.getSite(), userGroupRequest.getUserGroup(), userGroupRequest.getPermissionForActivityGroup());
                return ResponseEntity.ok(removeActivityGroup);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("getAvailableUserGroup")
    public ResponseEntity<List<AvailableUserGroup>> getAvailableUserGroup(@RequestBody UserGroupRequest userGroupRequest) {
        List<AvailableUserGroup> getAvailableUserGroup;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                getAvailableUserGroup = userGroupService.getAvailableUserGroup(userGroupRequest);
                return ResponseEntity.ok(getAvailableUserGroup);
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("isExist")
    public boolean isExist(@RequestBody UserGroupRequest userGroupRequest) {
        boolean isExist;
        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {

            try {
                isExist = userGroupService.isExist(userGroupRequest.getSite(), userGroupRequest.getUserGroup());
                return isExist;
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }

    @PostMapping("userGroupCopyForNewSite")
    public Boolean retrieveGlobalSite(@RequestBody UserGroupRequest userGroupRequest) {

        if (userGroupRequest.getSite() != null && !userGroupRequest.getSite().isEmpty()) {
            try {
                List<UserGroup> userGroup = userGroupService.UserGroupCopyForNewSite(userGroupRequest.getSite());
                if(userGroup.size()!=0){
                    return true;
                }
                else{
                return false;
                }
            } catch (UserGroupException userGroupException) {
                throw userGroupException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserGroupException(1);
    }
}
