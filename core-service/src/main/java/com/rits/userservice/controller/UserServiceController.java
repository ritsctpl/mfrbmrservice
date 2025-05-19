package com.rits.userservice.controller;

import com.rits.activitygroupservice.model.ActivityGroupMember;
import com.rits.activitygroupservice.service.ActivityGroupService;
import com.rits.activityservice.dto.ActivityRequest;
import com.rits.activityservice.model.Activity;
import com.rits.activityservice.service.ActivityService;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;
import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.usergroupservice.model.ActivityGroup;
import com.rits.usergroupservice.service.UserGroupService;
import com.rits.userservice.dto.*;
import com.rits.userservice.exception.UserException;
import com.rits.userservice.model.MessageDetails;
import com.rits.userservice.model.UserMessageModel;
import com.rits.userservice.model.User;
import com.rits.userservice.model.UserGroup;
import com.rits.userservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/user-service")
public class UserServiceController {

    private final UserServiceImpl userServiceImpl;
    private final UserGroupService userGroupService;
    private final AuditLogService auditlogservice;
    private final ActivityGroupService activityGroupService;
    private final ActivityService activityService;
    private final ApplicationEventPublisher eventPublisher;

    // Helper method to validate that the site is present.
    private void validateSite(UserRequest userRequest) {
        if (userRequest.getSite() == null || userRequest.getSite().isEmpty()) {
            throw new UserException(2503, userRequest.getSite());
        }
    }

    // Helper method to build an AuditLogRequest based on the userRequest and operation parameters.
    private AuditLogRequest buildAuditLog(UserRequest userRequest, String actionCode, String actionDetail, String category) {
        String timestamp = String.valueOf(LocalDateTime.now());
        return AuditLogRequest.builder()
                .site(userRequest.getCurrentSite())
                .action_code(actionCode)
                .action_detail(actionDetail)
                .action_detail_handle("ActionDetailBO:" + userRequest.getSite() + "," + actionCode + userRequest.getUser() + ":" + "com.rits.userservice.controller")
                .activity("From Service")
                .date_time(timestamp)
                .userId(userRequest.getUser())
                .txnId(actionCode + timestamp + userRequest.getUser())
                .created_date_time(timestamp)
                .category(category)
                .topic("audit-log")
                .build();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserMessageModel> createUser(@RequestBody UserRequest userRequest) {
        validateSite(userRequest);
        try {
            UserMessageModel createdUser = userServiceImpl.createUser(userRequest);
            AuditLogRequest auditlog = buildAuditLog(userRequest, "USER-CREATED", "User Created", "Create");
            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(createdUser);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserMessageModel> updateUser(@RequestBody UserRequest userRequest) {
        validateSite(userRequest);
        try {
            UserMessageModel updatedUser = userServiceImpl.updateUser(userRequest);
            AuditLogRequest auditlog = buildAuditLog(userRequest, "USER-UPDATED", "User Updated", "Update");
            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(updatedUser);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateUserWithOutUpdatingUserGroup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<User> updateUserWithOutUpdatingUserGroup(@RequestBody UserRequest userRequest) {
        validateSite(userRequest);
        try {
            User updatedUser = userServiceImpl.updateUserWithOutUpdatingUserGroup(userRequest);
            AuditLogRequest auditlog = buildAuditLog(userRequest, "USER-UPDATED", "User Updated", "Update");
            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(updatedUser);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserMessageModel> deleteUser(@RequestBody UserRequest userRequest) {
        validateSite(userRequest);
        try {
            UserMessageModel deletedRecord = userServiceImpl.deleteUser(userRequest);
            AuditLogRequest auditlog = buildAuditLog(userRequest, "USER-DELETED", "WorkCenter Deleted", "Delete");
            eventPublisher.publishEvent(new ProducerEvent(auditlog));
            return ResponseEntity.ok(deletedRecord);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveByUser")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<User> retrieveByUser(@RequestBody UserRequest userRequest) {
        try {
            User existingUserRecord = userServiceImpl.retrieveByUser(userRequest);
            return ResponseEntity.ok(existingUserRecord);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isUserExists(@RequestBody UserRequest userRequest) {
        try {
            Boolean isUserExists = userServiceImpl.isUserExists(userRequest);
            return ResponseEntity.ok(isUserExists);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserListResponse> retrieveTop50(@RequestBody UserRequest userRequest) {
        try {
            UserListResponse retrievedTop50Records = userServiceImpl.retrieveTop50(userRequest);
            return ResponseEntity.ok(retrievedTop50Records);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveAllByUser")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserListResponse> retrieveAllByUser(@RequestBody UserRequest userRequest) {
        try {
            UserListResponse allRetrievedRecordsByUser = userServiceImpl.retrieveAllByUser(userRequest);
            return ResponseEntity.ok(allRetrievedRecordsByUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve_detailed_user")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserResponse> retrieve_detailed_user(@RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = new UserResponse();
            User user = userServiceImpl.retrieveByUserOnDefaultSite(userRequest);
            userResponse.setSite(user.getSite());
            userResponse.setCurrentSite(user.getCurrentSite());
            userResponse.setUser(user.getUser());
            userResponse.setLastName(user.getLastName());
            userResponse.setStatus(user.getStatus());

            List<UserGroup> userUserGroup = user.getUserGroups();
            Set<String> activityGroupSet = new HashSet<>();
            for (UserGroup ug : userUserGroup) {
                com.rits.usergroupservice.model.UserGroup userGroup;
                if (userRequest.getCurrentSite() != null) {
                    userGroup = userGroupService.retrieveUserGroup(userRequest.getCurrentSite(), ug.getUserGroup());
                } else {
                    if (user.getCurrentSite() != null && !user.getCurrentSite().isEmpty()) {
                        userGroup = userGroupService.retrieveUserGroup(user.getCurrentSite(), ug.getUserGroup());
                    } else {
                        userGroup = userGroupService.retrieveUserGroup(user.getDefaultSite(), ug.getUserGroup());
                    }
                }
                userResponse.getUserGroups().add(ug);
                List<ActivityGroup> availableActivityGroup = filterEnabledActivityGroups(userGroup.getPermissionForActivityGroup());
                List<UserActivityGroupDetail> userActivityGroupDetails = new ArrayList<>();
                for (ActivityGroup activityGroup : availableActivityGroup) {
                    UserActivityGroupDetail userActivityGroupDetail = new UserActivityGroupDetail();
                    com.rits.activitygroupservice.model.ActivityGroup retrieveActivityGroup;
                    if (userRequest.getCurrentSite() != null) {
                        retrieveActivityGroup = activityGroupService.retrieveActivityGroup(
                                new com.rits.activitygroupservice.dto.ActivityGroupRequest(userRequest.getCurrentSite(), activityGroup.getActivityGroupName()));
                    } else {
                        if (user.getCurrentSite() != null && !user.getCurrentSite().isEmpty()) {
                            retrieveActivityGroup = activityGroupService.retrieveActivityGroup(
                                    new com.rits.activitygroupservice.dto.ActivityGroupRequest(user.getCurrentSite(), activityGroup.getActivityGroupName()));
                        } else {
                            retrieveActivityGroup = activityGroupService.retrieveActivityGroup(
                                    new com.rits.activitygroupservice.dto.ActivityGroupRequest(user.getDefaultSite(), activityGroup.getActivityGroupName()));
                        }
                    }
                    userActivityGroupDetail.setActivityGroup(retrieveActivityGroup.getActivityGroupName());
                    userActivityGroupDetail.setActivityGroupDescription(retrieveActivityGroup.getActivityGroupDescription());

                    if (retrieveActivityGroup.getActivityGroupMemberList() != null) {
                        List<String> allowedActivityIds = availableActivityGroup.stream()
                                .filter(group -> group != null && group.isEnabled())
                                .flatMap(group -> {
                                    List<com.rits.usergroupservice.model.Activity> activities = group.getPermissionForActivity();
                                    return activities != null ? activities.stream() : Stream.empty();
                                })
                                .map(activity -> (activity != null && activity.getActivityId() != null) ? activity.getActivityId() : null)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        List<ActivityGroupMember> activityList = retrieveActivityGroup.getActivityGroupMemberList();
                        activityList.removeIf(member -> !allowedActivityIds.contains(member.getActivityId()));

                        for (ActivityGroupMember activityID : activityList) {
                            try {
                                Activity activity;
                                if (userRequest.getCurrentSite() != null) {
                                    activity = activityService.retrieveActivity(
                                            new ActivityRequest(userRequest.getCurrentSite(), activityID.getActivityId()));
                                } else {
                                    if (user.getCurrentSite() != null && !user.getCurrentSite().isEmpty()) {
                                        activity = activityService.retrieveActivity(
                                                new ActivityRequest(user.getCurrentSite(), activityID.getActivityId()));
                                    } else {
                                        activity = activityService.retrieveActivity(
                                                new ActivityRequest(user.getDefaultSite(), activityID.getActivityId()));
                                    }
                                }
                                userActivityGroupDetail.getActivityList().add(activity);
                            } catch (Exception e) {
                                // Log exception if needed for debugging
                            }
                        }
                    }
                    userActivityGroupDetails.add(userActivityGroupDetail);
                }
                userResponse.getUserActivityGroupDetails().addAll(userActivityGroupDetails);
            }

            Map<String, UserActivityGroupDetail> mergedActivityGroups = new HashMap<>();
            for (UserActivityGroupDetail groupDetail : userResponse.getUserActivityGroupDetails()) {
                String activityGroup = groupDetail.getActivityGroup();
                if (mergedActivityGroups.containsKey(activityGroup)) {
                    UserActivityGroupDetail existingDetail = mergedActivityGroups.get(activityGroup);
                    Set<String> existingActivityIds = existingDetail.getActivityList().stream()
                            .map(Activity::getActivityId)
                            .collect(Collectors.toSet());
                    for (Activity activity : groupDetail.getActivityList()) {
                        if (!existingActivityIds.contains(activity.getActivityId())) {
                            existingDetail.getActivityList().add(activity);
                            existingActivityIds.add(activity.getActivityId());
                        }
                    }
                } else {
                    mergedActivityGroups.put(activityGroup, groupDetail);
                }
            }
            List<UserActivityGroupDetail> mergedGroupList = new ArrayList<>(mergedActivityGroups.values());
            userResponse.setUserActivityGroupDetails(mergedGroupList);
            return ResponseEntity.ok(userResponse);
        } catch (UserException | UserGroupException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ActivityGroup> filterEnabledActivityGroups(List<ActivityGroup> activityGroups) {
        if (activityGroups == null) {
            return new ArrayList<>();
        }
        Iterator<ActivityGroup> groupIterator = activityGroups.iterator();
        while (groupIterator.hasNext()) {
            ActivityGroup group = groupIterator.next();
            if (group == null || !group.isEnabled()) {
                groupIterator.remove();
            } else {
                List<com.rits.usergroupservice.model.Activity> activities = group.getPermissionForActivity();
                if (activities != null) {
                    activities.removeIf(activity -> activity == null || !activity.isEnabled());
                }
            }
        }
        return activityGroups;
    }

    @PostMapping("/availableUsers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AllUserResponse> availableUsers(@RequestBody UserRequest userRequest) {
        try {
            AllUserResponse allAvailableUser = userServiceImpl.availableUsers(userRequest);
            return ResponseEntity.ok(allAvailableUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/availableUserGroup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableUserGroups> availableUserGroup(@RequestBody UserRequest userRequest) {
        try {
            AvailableUserGroups availableUserGroups = userServiceImpl.availableUserGroup(userRequest);
            return ResponseEntity.ok(availableUserGroups);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/availableWorkCenter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AvailableWorkCenterList> getAllAvailableWorkCenter(@RequestBody UserRequest userRequest) {
        try {
            AvailableWorkCenterList availableWorkCenterList = userServiceImpl.getAllAvailableWorkCenter(userRequest);
            return ResponseEntity.ok(availableWorkCenterList);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateLastDefaultSite")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> updateLastDefaultSite(@RequestBody UserRequest userRequest) {
        try {
            Boolean flag = userServiceImpl.updateDefaultSite(userRequest.getUser(), userRequest.getDefaultSite());
            return ResponseEntity.ok(flag);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/addUserGroup")
    @ResponseStatus(HttpStatus.OK)
    public Boolean addUserGroup(@RequestBody UserRequest userRequest) {
        try {
            Boolean updatedUser = userServiceImpl.addUserGroup(userRequest, getUserGroupList(userRequest));
            return updatedUser;
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/removeUserGroup")
    @ResponseStatus(HttpStatus.OK)
    public Boolean removeUserGroup(@RequestBody UserRequest userRequest) {
        try {
            Boolean updatedUser = userServiceImpl.removeUserGroup(userRequest, getUserGroupList(userRequest));
            return updatedUser;
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getSiteListByUser")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getSiteListByUser(@RequestBody User user) {
        try {
            return userServiceImpl.getSiteListByUser(user);
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to extract the list of user group names from a UserRequest.
    private List<String> getUserGroupList(UserRequest userRequest) {
        return userRequest.getUserGroups().stream()
                .map(UserGroup::getUserGroup)
                .collect(Collectors.toList());
    }
}
