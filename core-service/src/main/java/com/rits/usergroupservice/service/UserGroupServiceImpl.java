package com.rits.usergroupservice.service;


import com.rits.activitygroupservice.model.ActivityGroupMember;
import com.rits.activitygroupservice.service.ActivityGroupServiceImpl;
import com.rits.site.repository.SiteRepository;
import com.rits.usergroupservice.dto.*;
import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.usergroupservice.model.*;
import com.rits.userservice.dto.UserRequest;
import com.rits.usergroupservice.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Lazy
public class UserGroupServiceImpl implements UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    private final ActivityGroupServiceImpl activityGroupService;

    @Value("${pod-service.url}/isExist")
    private String isPodExist;
    @Value("${user-service.url}/availableUsers")
    private String userUrl;

    @Value("${user-service.url}/addUserGroup")
    private String addUserGroup;

    @Value("${user-service.url}/removeUserGroup")
    private String removeUserGroup;

    @Value("${activity-service.url}/retrieveAll")
    private String activityUrl;
    @Value("${activitygroup-service.url}/retrieveBySite")
    private String activityGroupUrl;
    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public UserGroupMessageModel createUserGroup(UserGroupRequest userGroupRequest) throws Exception {
        if (userGroupRepository.existsByActiveAndSiteAndUserGroup(1, userGroupRequest.getSite(), userGroupRequest.getUserGroup())) {
            throw new UserGroupException(3001, userGroupRequest.getUserGroup());
        } else {
            if (userGroupRequest.getPod() != null && !userGroupRequest.getPod().isEmpty()) {
                IsExist isExist = IsExist.builder().site(userGroupRequest.getSite()).podName(userGroupRequest.getPod()).build();
                Boolean podExist = isPodExist(isExist);
                if (!podExist) {
                    throw new UserGroupException(1300, userGroupRequest.getPod());
                }
            }

            if (userGroupRequest.getDescription() == null || userGroupRequest.getDescription().isEmpty()) {
                userGroupRequest.setDescription(userGroupRequest.getUserGroup());
            }
            UserGroup userGroup = userGroupBuilder(userGroupRequest);

            userGroup.setHandle("UserGroupBO:" + userGroupRequest.getSite() + "," + userGroupRequest.getUserGroup());
            userGroup.setCreatedBy(userGroupRequest.getUserId());
            userGroup.setCreatedDateTime(LocalDateTime.now());
            List<com.rits.userservice.model.UserGroup> userGroupList = new ArrayList<>();
            com.rits.userservice.model.UserGroup userGroupreq = com.rits.userservice.model.UserGroup.builder().userGroup(userGroupRequest.getUserGroup()).build();
            userGroupList.add(userGroupreq);
            if (userGroupRequest.getUsers() != null && !userGroupRequest.getUsers().isEmpty()) {
                for (User user : userGroupRequest.getUsers()) {
                    List<String> userGroups = new ArrayList<>();
                    userGroups.add(userGroupreq.getUserGroup());

                    UserRequest addUserRequest = UserRequest.builder()
                            .user(user.getUser())
                            .userGroups(userGroupList)
                            .build();

                    Boolean addUserGroupNam = addUserGroup(addUserRequest);
                    if (addUserGroupNam == null) {
                        throw new UserGroupException(3002, userGroupreq.getUserGroup());
                    }
                }

            }
            String createdMessage = getFormattedMessage(1, userGroupRequest.getUserGroup());
            return UserGroupMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(userGroupRepository.save(userGroup)).build();
        }
    }

    @Override
    public UserGroupMessageModel updateUserGroup(UserGroupRequest userGroupRequest) throws Exception {
        if (userGroupRepository.existsByActiveAndSiteAndUserGroup(1, userGroupRequest.getSite(), userGroupRequest.getUserGroup())) {
            UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, userGroupRequest.getSite(), userGroupRequest.getUserGroup());

            if (userGroupRequest.getPod() != null && !userGroupRequest.getPod().isEmpty()) {
                IsExist isExist = IsExist.builder().site(userGroupRequest.getSite()).podName(userGroupRequest.getPod()).build();
                Boolean podExist = isPodExist(isExist);
                if (!podExist) {
                    throw new UserGroupException(1300, userGroupRequest.getPod());
                }
            }
            if (userGroupRequest.getDescription() == null || userGroupRequest.getDescription().isEmpty()) {
                userGroupRequest.setDescription(userGroupRequest.getUserGroup());
            }

            List<User> updatedUserList = userGroupRequest.getUsers();
            List<User> existingUserList = existingUserGroup.getUsers();

            if (existingUserList != null && !existingUserList.isEmpty()) {

                List<String> userToAdd = new ArrayList<>();
                List<String> userToRemove = new ArrayList();

                for (User updatedUser : updatedUserList) {
                    boolean alreadyExists = existingUserList.stream().anyMatch(user -> user.getUser().equals(updatedUser.getUser()));
                    if (!alreadyExists) {
                        userToAdd.add(updatedUser.getUser());
                    }
                }

                for (User existingUserToremove : existingUserList) {
                    boolean isRemoved = updatedUserList.stream().noneMatch(user -> user.getUser().equals(existingUserToremove.getUser()));
                    if (isRemoved) {
                        userToRemove.add(existingUserToremove.getUser());
                    }
                }
                List<com.rits.userservice.model.UserGroup> userGroupAddList = new ArrayList<>();
                com.rits.userservice.model.UserGroup userGroupLists = new com.rits.userservice.model.UserGroup(userGroupRequest.getUserGroup());
                userGroupAddList.add(userGroupLists);

                if (!userToAdd.isEmpty()) {
                    for (String userName : userToAdd) {
                        UserRequest addUserRequest = UserRequest.builder()
                                .user(userName)
                                .userGroups(userGroupAddList)
                                .build();

                        Boolean addUserGroupNam = addUserGroup(addUserRequest);
                        if (addUserGroupNam == null) {
                            throw new UserGroupException(3002, userGroupRequest.getUserGroup());
                        }
                    }
                }
                List<com.rits.userservice.model.UserGroup> userGroupRemovList = new ArrayList<>();
                com.rits.userservice.model.UserGroup userGroupRemLists = new com.rits.userservice.model.UserGroup(userGroupRequest.getUserGroup());
                userGroupRemovList.add(userGroupRemLists);

                if (!userToRemove.isEmpty()) {
                    for (String userName : userToRemove) {
                        UserRequest removeUserRequest = UserRequest.builder()
                                .user(userName)
                                .userGroups(userGroupRemovList)
                                .build();

                        Boolean removeUserGroupNam = removeUserGroup(removeUserRequest);

                        if (removeUserGroupNam == null) {
                            throw new UserGroupException(3002, userGroupRequest.getUserGroup());
                        }
                    }
                }
            } else {
                List<com.rits.userservice.model.UserGroup> userGroupList = new ArrayList<>();
                com.rits.userservice.model.UserGroup userGroupreq = com.rits.userservice.model.UserGroup.builder().userGroup(userGroupRequest.getUserGroup()).build();
                userGroupList.add(userGroupreq);

                if (!userGroupRequest.getUsers().isEmpty()) {
                    for (User user : userGroupRequest.getUsers()) {
                        List<String> userGroups = new ArrayList<>();
                        userGroups.add(userGroupreq.getUserGroup());

                        UserRequest addUserRequest = UserRequest.builder()
                                .user(user.getUser())
                                .userGroups(userGroupList)
                                .build();

                        Boolean addUserGroupNam = addUserGroup(addUserRequest);

                        if (addUserGroupNam == null) {
                            throw new UserGroupException(3002, userGroupreq.getUserGroup());
                        }
                    }
                }
            }
            UserGroup userGroup = userGroupBuilder(userGroupRequest);
            userGroup.setSite(existingUserGroup.getSite());
            userGroup.setHandle(existingUserGroup.getHandle());
            userGroup.setCreatedBy(existingUserGroup.getCreatedBy());
            userGroup.setCreatedDateTime(existingUserGroup.getCreatedDateTime());
            userGroup.setUserGroup(existingUserGroup.getUserGroup());
            userGroup.setModifiedBy(userGroupRequest.getUserId());
            userGroup.setModifiedDateTime(LocalDateTime.now());
            String createdMessage = getFormattedMessage(2, userGroupRequest.getUserGroup());
            return UserGroupMessageModel.builder()
                    .message_details(new MessageDetails(createdMessage, "S"))
                    .response(userGroupRepository.save(userGroup))
                    .build();
        } else {
            throw new UserGroupException(3002, userGroupRequest.getUserGroup());
        }
    }

    public void userGroupUpdate(UserGroup existingUserGroup, UserGroupRequest userGroupRequest){
        existingUserGroup.setSite(userGroupRequest.getSite());
        existingUserGroup.setUserGroup(userGroupRequest.getUserGroup());
        existingUserGroup.setDescription(userGroupRequest.getDescription());
        existingUserGroup.setPod(userGroupRequest.getPod());

        userUpdate(existingUserGroup, userGroupRequest);
//        activityUpdate(existingUserGroup, userGroupRequest);
        activityGroupUpdate(existingUserGroup, userGroupRequest);
        customDataUpdate(existingUserGroup, userGroupRequest);
    }

    public void userUpdate(UserGroup existingUserGroup, UserGroupRequest userGroupRequest){
        if(userGroupRequest.getUsers() == null){
            existingUserGroup.setUsers(null);
            return;
        }

        if(existingUserGroup.getUsers() == null){
            List<User> user = new ArrayList<>(userGroupRequest.getUsers());
            existingUserGroup.setUsers(user);

        } else {
            existingUserGroup.getUsers().clear();
            existingUserGroup.getUsers().addAll(userGroupRequest.getUsers());
        }
    }

    public void activityGroupUpdate(UserGroup existingUserGroup, UserGroupRequest userGroupRequest){

        if(userGroupRequest.getPermissionForActivityGroup() == null){
            existingUserGroup.setPermissionForActivityGroup(null);
            return;
        }

        if(existingUserGroup.getPermissionForActivityGroup() == null){
            List<ActivityGroup> activityGroups = new ArrayList<>(userGroupRequest.getPermissionForActivityGroup());
            existingUserGroup.setPermissionForActivityGroup(activityGroups);

        } else {
            existingUserGroup.getPermissionForActivityGroup().clear();
            existingUserGroup.getPermissionForActivityGroup().addAll(userGroupRequest.getPermissionForActivityGroup());
        }
    }

    public void customDataUpdate(UserGroup existingUserGroup, UserGroupRequest userGroupRequest){

        if(userGroupRequest.getCustomDataList() == null){
            existingUserGroup.setCustomDataList(null);
            return;
        }

        if(existingUserGroup.getCustomDataList() == null){
            List<CustomData> customDataList = new ArrayList<>(userGroupRequest.getCustomDataList());
            existingUserGroup.setCustomDataList(customDataList);

        } else {
            existingUserGroup.getCustomDataList().clear();
            existingUserGroup.getCustomDataList().addAll(userGroupRequest.getCustomDataList());
        }
    }

    @Override
    public UserGroup retrieveUserGroup(String site, String userGroup) throws Exception {
        if (userGroupRepository.existsByActiveAndSiteAndUserGroup(1, site, userGroup)) {
            UserGroup retrievedUserGrp = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);

            List<ActivityGroup> activityGroupList = new ArrayList<>();
            List<com.rits.activitygroupservice.model.ActivityGroup> allActivityGroups = activityGroupService.retrieveAllBySite();
            List<ActivityGroup> retrievedPermissionForActivityGrp = retrievedUserGrp.getPermissionForActivityGroup();

            if (retrievedPermissionForActivityGrp == null) {
                retrievedPermissionForActivityGrp = new ArrayList<>();
            }

            for (com.rits.activitygroupservice.model.ActivityGroup allActivityGroup : allActivityGroups) {
                if (allActivityGroup == null || allActivityGroup.getActivityGroupName() == null) {
                    continue;
                }

                boolean matchFound = false;

                for (ActivityGroup retrievedActivityGrp : retrievedPermissionForActivityGrp) { // Old
                    if (retrievedActivityGrp == null || retrievedActivityGrp.getActivityGroupName() == null) {
                        continue;
                    }

                    if (allActivityGroup.getActivityGroupName().equalsIgnoreCase(retrievedActivityGrp.getActivityGroupName())) {
                        matchFound = true;
                        boolean mainEnable = false;
                        ActivityGroup newActivityGrp = new ActivityGroup();
                        newActivityGrp.setActivityGroupName(retrievedActivityGrp.getActivityGroupName());
                        newActivityGrp.setActivityGroupDescription(retrievedActivityGrp.getActivityGroupDescription());

                        List<Activity> newActivityList = new ArrayList<>();

                        List<Activity> retrievedActivities = retrievedActivityGrp.getPermissionForActivity();
                        if (retrievedActivities == null) {
                            for (ActivityGroupMember activityGroupMember : allActivityGroup.getActivityGroupMemberList()) {
                                if (activityGroupMember == null || activityGroupMember.getActivityId() == null) {
                                    continue;
                                }
                                newActivityList.add(new Activity(activityGroupMember.getActivityId(), false)); // Default to 'false'
                            }
                        } else {
                            for (ActivityGroupMember activityGroupMember : allActivityGroup.getActivityGroupMemberList()) {
                                if (activityGroupMember == null || activityGroupMember.getActivityId() == null) {
                                    continue;
                                }

                                boolean activityMatchFound = false;

                                for (Activity activity : retrievedActivities) {
                                    if (activity != null && activityGroupMember.getActivityId().equals(activity.getActivityId())) {
                                        newActivityList.add(new Activity(activity.getActivityId(), activity.isEnabled()));
                                        activityMatchFound = true;
                                        mainEnable = true;
                                        break;
                                    }
                                }

                                if (!activityMatchFound) {
                                    newActivityList.add(new Activity(activityGroupMember.getActivityId(), false));
                                }
                            }
                        }

                        newActivityGrp.setPermissionForActivity(newActivityList);
                        newActivityGrp.setEnabled(retrievedActivityGrp.isEnabled());
                        activityGroupList.add(newActivityGrp);
                        break;
                    }
                }

                // If no match was found, add the current allActivityGroup as a new ActivityGroup
                if (!matchFound) {
                    ActivityGroup newActivityGrp = new ActivityGroup();
                    newActivityGrp.setActivityGroupName(allActivityGroup.getActivityGroupName());
                    newActivityGrp.setActivityGroupDescription(allActivityGroup.getActivityGroupDescription());

                    List<Activity> newActivityList = new ArrayList<>();
                    for (ActivityGroupMember activityGroupMember : allActivityGroup.getActivityGroupMemberList()) {
                        if (activityGroupMember == null || activityGroupMember.getActivityId() == null) {
                            continue;
                        }
                        newActivityList.add(new Activity(activityGroupMember.getActivityId(), false)); // Default to 'false'
                    }

                    newActivityGrp.setPermissionForActivity(newActivityList);
                    newActivityGrp.setEnabled(false); // Default to disabled
                    activityGroupList.add(newActivityGrp);
                }
            }

            retrievedUserGrp.setPermissionForActivityGroup(activityGroupList);

            return retrievedUserGrp;
        } else {
            throw new UserGroupException(3002, userGroup);
        }
    }


    @Override
    public boolean isExist(String site, String usergroup) {
        return userGroupRepository.existsByActiveAndSiteAndUserGroup(1, site, usergroup);
    }


    @Override
    public UserGroupResponseList getAllUserGroup(String site, String userGroup) throws Exception {
        if (userGroup != null && !userGroup.isEmpty()) {
            List<UserGroupResponse> userGroupResponse = userGroupRepository.findByActiveAndSiteAndUserGroupContainingIgnoreCase(1, site, userGroup);
            if (userGroupResponse == null || userGroupResponse.isEmpty()) {
                throw new UserGroupException(3002, userGroup);
            }
            return UserGroupResponseList.builder().userGroupResponses(userGroupResponse).build();
        }
        return getAllUserGroupByCreationDate(site);
    }

    @Override
    public UserGroupResponseList getAllUserGroupByCreationDate(String site) throws Exception {
        List<UserGroupResponse> userGroupResponse = userGroupRepository.findByActiveAndSiteOrderByCreatedDateTimeDesc(1, site);

        return UserGroupResponseList.builder().userGroupResponses(userGroupResponse).build();
    }

    @Override
    public UserGroupMessageModel deleteUserGroup(String site, String userGroup, String userId) throws Exception {
        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup != null) {
            existingUserGroup.setActive(0);
            existingUserGroup.setModifiedDateTime(LocalDateTime.now());
            existingUserGroup.setModifiedBy(userId);
            if(!existingUserGroup.getUsers().isEmpty() && existingUserGroup.getUserGroup() != null)
            {
                List<com.rits.userservice.model.UserGroup> userGroupRemovList = new ArrayList<>();
                com.rits.userservice.model.UserGroup userGroupRemLists = new com.rits.userservice.model.UserGroup(existingUserGroup.getUserGroup());
                userGroupRemovList.add(userGroupRemLists);
                for (User userName : existingUserGroup.getUsers()) {
                    UserRequest removeUserRequest = UserRequest.builder()
                            .user(userName.getUser())
                            .userGroups(userGroupRemovList)
                            .build();
                    Boolean removeUserGroupNam = removeUserGroup(removeUserRequest);
                }
            }
            userGroupRepository.save(existingUserGroup);
            String createdMessage = getFormattedMessage(3, userGroup);
            return UserGroupMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).build();

        }
        throw new UserGroupException(3002, userGroup);
    }

    @Override
    public List<User> getAvailableUser(String site, String userGroup) throws Exception {
        UserServiceRequest request = UserServiceRequest.builder().site(Collections.singletonList(site)).build();
        UserList userResponse = availableUsers(request);
        if (userResponse == null) {
            throw new UserGroupException(2500);
        }
        List<User> users = userResponse.getAvailableUsers();
        if (userGroup != null && !userGroup.isEmpty()) {
            UserGroup existingUsergroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
            if (existingUsergroup == null) {
                throw new UserGroupException(3000, userGroup);
            }
            List<User> existingUsers = existingUsergroup.getUsers();
            if (existingUsers != null && !existingUsers.isEmpty()) {
                users.removeIf(user -> existingUsers.stream().anyMatch(matchingUser -> matchingUser.getUser().equals(user.getUser())));
                return users;
            }
        }
        return users;
    }

    @Override
    public List<Activity> getAvailableActivity(String site, String userGroup) throws Exception {

        IsExist isExist = IsExist.builder().site(site).build();
        List<Activity> activityResponse = retrieveAllActivity(isExist);

        if (activityResponse == null || activityResponse.isEmpty()) {
            throw new UserGroupException(2500);
        }
        if (userGroup != null && !userGroup.isEmpty()) {
            UserGroup existingUsergroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
            if (existingUsergroup == null) {
                throw new UserGroupException(3000, userGroup);
            }

            if(existingUsergroup.getPermissionForActivityGroup() != null) {
                List<Activity> existingActivity = existingUsergroup.getPermissionForActivityGroup().stream()
                        .flatMap(activityGroup -> activityGroup.getPermissionForActivity().stream())
                        .collect(Collectors.toList());

                if (!existingActivity.isEmpty()) {
                    activityResponse.removeIf(activity -> existingActivity.stream().anyMatch(activities -> activities.getActivityId().equals(activity.getActivityId())));
                    return activityResponse;
                }
            }
        }
        return activityResponse;
    }

    @Override
    public List<ActivityGroup> getAvailableActivityGroup(String site, String userGroup) throws Exception {

        IsExist isExist = IsExist.builder().site(site).build();
        List<ActivityGroup> activityGroupResponse = retrieveActivityGroupBySite(isExist);

        if (activityGroupResponse == null) {
            throw new UserGroupException(2500);
        }
        if (userGroup != null && !userGroup.isEmpty()) {
            UserGroup existingUsergroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
            if (existingUsergroup == null) {
                throw new UserGroupException(3000, userGroup);
            }
            List<ActivityGroup> existingActivityGroups = existingUsergroup.getPermissionForActivityGroup();
            if (existingActivityGroups != null && !existingActivityGroups.isEmpty()) {

                activityGroupResponse.removeIf(activityGroup -> existingActivityGroups.stream().anyMatch(activityGroups -> activityGroups.getActivityGroupName().equals(activityGroup.getActivityGroupName())));
                return activityGroupResponse;
            }
        }
        return activityGroupResponse;
    }

    @Override
    public Boolean assignUser(String site, String userGroup, List<User> users) throws Exception {
        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }
        List<User> existingUsers = existingUserGroup.getUsers();
        if (existingUsers.isEmpty()) {
            existingUsers.addAll(users);
        } else {
            for (User user : users) {
                boolean alreadyExists = existingUsers.stream().anyMatch(addUser -> Objects.equals(addUser.getUser(), user.getUser()));
                if (!alreadyExists) {
                    existingUsers.add(user);
                }
            }
        }
        existingUserGroup.setUsers(existingUsers);
        existingUserGroup.setModifiedDateTime(LocalDateTime.now());
        userGroupRepository.save(existingUserGroup);
        return true;
    }

    @Override
    public Boolean removeUser(String site, String userGroup, List<User> users) throws Exception {
        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }
        List<User> existingUsers = existingUserGroup.getUsers();
        if (existingUsers == null) {
            return null;
        }
        for (User user : users) {
            existingUsers.removeIf(existingUser -> existingUser.getUser().equals(user.getUser()));
        }
        existingUserGroup.setUsers(existingUsers);
        existingUserGroup.setModifiedDateTime(LocalDateTime.now());
        userGroupRepository.save(existingUserGroup);
        return true;
    }
    @Override
    public List<UserGroup> UserGroupCopyForNewSite(String site) {

        List<UserGroup> parentUserGroups = userGroupRepository.findBySiteAndActive("*", 1);
        List<UserGroup> copiedUsergroups=new ArrayList<UserGroup>();

        if (parentUserGroups.size()!=0) {
            for (UserGroup userGroup : parentUserGroups) {
                // Update the `site` field
                userGroup.setSite(site);
                userGroup.setHandle("UserGroupBO,"+site+","+userGroup.getUserGroup());
                userGroup.setCreatedBy("SYSUSER");
                copiedUsergroups.add(userGroup);
            }
            userGroupRepository.saveAll(copiedUsergroups);
            return copiedUsergroups;
        } else {

            throw new UserGroupException(3002, "");
        }
    }

    public List<String> extractUserGroups(List<UserGroup> parentUserGroups) {
        List<String> userGroups = new ArrayList<>();

        for (UserGroup userGroup : parentUserGroups) {
            userGroups.add(userGroup.getUserGroup());
        }

        return userGroups;
    }

    public List<ActivityGroup> extractActivityGroups(List<UserGroup> parentUserGroups) {
        List<ActivityGroup> permissionForActivityGroup = new ArrayList<>();

        for (UserGroup userGroup : parentUserGroups) {
            for (ActivityGroup parentActivityGroup : userGroup.getPermissionForActivityGroup()) {
                ActivityGroup newActivityGroup = new ActivityGroup(parentActivityGroup.getActivityGroupName());
                permissionForActivityGroup.add(newActivityGroup);
            }
        }

        return permissionForActivityGroup;
    }


    @Override
    public List<Activity> assignActivity(UserGroupRequest userGroupRequest) throws Exception {
        String site = userGroupRequest.getSite();
        String userGroup = userGroupRequest.getUserGroup();

        List<Activity> activities = new ArrayList<>();

        if(userGroupRequest.getPermissionForActivityGroup() != null){
            boolean allActivitiesPresent = userGroupRequest.getPermissionForActivityGroup().stream().allMatch(activityGroup -> activityGroup.getPermissionForActivity() != null);

            if(allActivitiesPresent)
                activities = userGroupRequest.getPermissionForActivityGroup().stream().flatMap(activityGroup -> activityGroup.getPermissionForActivity().stream()).collect(Collectors.toList());
        }

        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }

        if (existingUserGroup.getPermissionForActivityGroup() != null) {

            List<Activity> existingActivities = existingUserGroup.getPermissionForActivityGroup().stream()
                    .flatMap(activity -> activity.getPermissionForActivity().stream())
                    .collect(Collectors.toList());

            if (existingActivities.isEmpty()) {
                existingActivities.addAll(activities);
            } else {
                for (Activity activity : activities) {
                    boolean alreadyExists = existingActivities.stream().anyMatch(addActivities -> Objects.equals(addActivities.getActivityId(), activity.getActivityId()));
                    if (!alreadyExists) {
                        existingActivities.add(activity);
                    }
                }
            }
            existingUserGroup.getPermissionForActivityGroup().forEach(activityGroup -> {
                activityGroup.setPermissionForActivity(existingActivities);
            });

//            existingUserGroup.setPermissionForActivity(existingActivities);
            existingUserGroup.setModifiedDateTime(LocalDateTime.now());
            userGroupRepository.save(existingUserGroup);
        }
        return new ArrayList<>();
    }


    @Override
    public List<Activity> removeActivity(UserGroupRequest userGroupRequest) throws Exception {

        String site = userGroupRequest.getSite();
        String userGroup = userGroupRequest.getUserGroup();

        List<Activity> activities = new ArrayList<>();

        if(userGroupRequest.getPermissionForActivityGroup() != null){
            boolean allActivitiesPresent = userGroupRequest.getPermissionForActivityGroup().stream().allMatch(activityGroup -> activityGroup.getPermissionForActivity() != null);

            if(allActivitiesPresent)
                activities = userGroupRequest.getPermissionForActivityGroup().stream().flatMap(activityGroup -> activityGroup.getPermissionForActivity().stream()).collect(Collectors.toList());
        }

        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }
        if (existingUserGroup.getPermissionForActivityGroup() != null) {

            List<Activity> existingActivities = existingUserGroup.getPermissionForActivityGroup().stream()
                    .flatMap(activity -> activity.getPermissionForActivity().stream())
                    .collect(Collectors.toList());

            for (Activity activity : activities) {
                existingActivities.removeIf(existingActivity -> existingActivity.getActivityId().equals(activity.getActivityId()));
            }

            existingUserGroup.getPermissionForActivityGroup().forEach(activityGroup -> {
                activityGroup.setPermissionForActivity(existingActivities);
            });

            existingUserGroup.setModifiedDateTime(LocalDateTime.now());
            userGroupRepository.save(existingUserGroup);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ActivityGroup> assignActivityGroup(String site, String userGroup, List<ActivityGroup> activityGroups) throws Exception {
        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }
        List<ActivityGroup> existingActivityGroup = existingUserGroup.getPermissionForActivityGroup();
        if (existingActivityGroup.isEmpty()) {
            existingActivityGroup.addAll(activityGroups);
        } else {
            for (ActivityGroup activityGroup : activityGroups) {
                boolean alreadyExists = existingActivityGroup.stream().anyMatch(addActivityGroup -> Objects.equals(addActivityGroup.getActivityGroupName(), activityGroup.getActivityGroupName()));
                if (!alreadyExists) {
                    existingActivityGroup.add(activityGroup);
                }
            }
        }
        existingUserGroup.setPermissionForActivityGroup(existingActivityGroup);
        existingUserGroup.setModifiedDateTime(LocalDateTime.now());
        userGroupRepository.save(existingUserGroup);
        return existingActivityGroup;
    }

    @Override
    public List<ActivityGroup> removeActivityGroup(String site, String userGroup, List<ActivityGroup> activityGroups) throws Exception {
        UserGroup existingUserGroup = userGroupRepository.findByActiveAndSiteAndUserGroup(1, site, userGroup);
        if (existingUserGroup == null) {
            throw new UserGroupException(3002, userGroup);
        }
        List<ActivityGroup> existingActivityGroups = existingUserGroup.getPermissionForActivityGroup();
        if (existingActivityGroups == null) {
            return null;
        }
        for (ActivityGroup activityGroup : activityGroups) {
            existingActivityGroups.removeIf(existingActivityGroup -> existingActivityGroup.getActivityGroupName().equals(activityGroup.getActivityGroupName()));
        }
        existingUserGroup.setPermissionForActivityGroup(existingActivityGroups);
        existingUserGroup.setModifiedDateTime(LocalDateTime.now());
        userGroupRepository.save(existingUserGroup);
        return existingActivityGroups;
    }

    @Override
    public String callExtension(Extension extension) {
        String extensionResponse = webClientBuilder.build()
                .post()
                .uri(extensionUrl)
                .bodyValue(extension)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (extensionResponse == null) {
            throw new UserGroupException(800);
        }
        return extensionResponse;
    }

    @Override
    public List<AvailableUserGroup> getAvailableUserGroup(UserGroupRequest userGroupRequest) {
        return userGroupRepository.findByActiveAndSite(1, userGroupRequest.getSite());
    }

    public Boolean isPodExist(IsExist isExist) {
        Boolean podExist = webClientBuilder.build()
                .post()
                .uri(isPodExist)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return podExist;
    }

    public UserList availableUsers(UserServiceRequest request) {
        UserList userResponse = webClientBuilder.build()
                .post()
                .uri(userUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserList.class)
                .block();
        return userResponse;
    }

    public Boolean addUserGroup(UserRequest addUserRequest) {
        Boolean addUserGroupNam = webClientBuilder.build()
                .post()
                .uri(addUserGroup)
                .bodyValue(addUserRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return addUserGroupNam;
    }

    public Boolean removeUserGroup(UserRequest removeUserRequest) {
        Boolean removeUserGroupNam = webClientBuilder.build()
                .post()
                .uri(removeUserGroup)
                .bodyValue(removeUserRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return removeUserGroupNam;
    }

    public List<Activity> retrieveAllActivity(IsExist isExist) {
        List<Activity> activityResponse = webClientBuilder.build()
                .post()
                .uri(activityUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Activity>>() {
                })
                .block();
        return activityResponse;
    }

    public List<ActivityGroup> retrieveActivityGroupBySite(IsExist isExist) {
        List<ActivityGroup> activityGroupResponse = webClientBuilder.build()
                .post()
                .uri(activityGroupUrl)
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ActivityGroup>>() {})
                .block();

        if (activityGroupResponse != null) {
            for (ActivityGroup activityGroup : activityGroupResponse) {
                if (activityGroup.getActivityGroupMemberList() != null) {
                    List<Activity> activities = activityGroup.getActivityGroupMemberList().stream()
                            .filter(Objects::nonNull)
                            .map(member -> {
                                Activity activity = new Activity();
                                activity.setActivityId(member.getActivityId());
                                return activity;
                            })
                            .collect(Collectors.toList());

                    activityGroup.setPermissionForActivity(activities);
                }
            }
        }

        return activityGroupResponse;
    }

    public UserGroup userGroupBuilder(UserGroupRequest userGroupRequest) {
        UserGroup userGroup = UserGroup.builder()
                .site(userGroupRequest.getSite())
                .userGroup(userGroupRequest.getUserGroup())
                .description(userGroupRequest.getDescription())
                .pod(userGroupRequest.getPod())
                .users(userGroupRequest.getUsers())
//                .permissionForActivity(userGroupRequest.getPermissionForActivity())
                .permissionForActivityGroup(userGroupRequest.getPermissionForActivityGroup())
                .customDataList(userGroupRequest.getCustomDataList())
                .active(1)
                .build();
        if(userGroupRequest.getUsers() == null)
        {
            List<User> newuserList = new ArrayList<>();
            userGroupRequest.setUsers(newuserList);
        }
        return userGroup;
    }
}
