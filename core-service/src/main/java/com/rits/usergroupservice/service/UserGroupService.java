package com.rits.usergroupservice.service;

import com.rits.usergroupservice.dto.*;
import com.rits.usergroupservice.model.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserGroupService {


    public UserGroupMessageModel createUserGroup(UserGroupRequest userGroupRequest) throws Exception;

    public UserGroupMessageModel updateUserGroup(UserGroupRequest userGroupRequest) throws Exception;

    public UserGroup retrieveUserGroup(String site, String userGroup) throws Exception;

    boolean isExist(String site, String usergroup);

    public UserGroupResponseList getAllUserGroup(String site, String userGroup) throws Exception;

    public UserGroupResponseList getAllUserGroupByCreationDate(String site) throws Exception;

    public UserGroupMessageModel deleteUserGroup(String site, String userGroup, String userId) throws Exception;

    public List<User> getAvailableUser(String site, String userGroup) throws Exception;

    public List<Activity> getAvailableActivity(String site, String userGroup) throws Exception;

    public List<ActivityGroup> getAvailableActivityGroup(String site, String userGroup) throws Exception;

    public Boolean assignUser(String site, String userGroup, List<User> users) throws Exception;

    public Boolean removeUser(String site, String userGroup, List<User> users) throws Exception;

    public List<Activity> assignActivity(UserGroupRequest userGroupRequest) throws Exception;

    public List<Activity> removeActivity(UserGroupRequest userGroupRequest) throws Exception;

    public List<ActivityGroup> assignActivityGroup(String site, String userGroup, List<ActivityGroup> activityGroups) throws Exception;

    public List<ActivityGroup> removeActivityGroup(String site, String userGroup, List<ActivityGroup> activityGroups) throws Exception;

    public String callExtension(Extension extension);

    public List<AvailableUserGroup> getAvailableUserGroup(UserGroupRequest userGroupRequest);

    List<UserGroup> UserGroupCopyForNewSite(String site);

//   List<com.rits.userservice.model.UserGroup> getAllUserGroups();
//
//    void assignUserGroupToSite(com.rits.userservice.model.UserGroup userGroup);
}
