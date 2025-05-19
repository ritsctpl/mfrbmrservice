package com.rits.userservice.service;

import com.rits.userservice.dto.*;
import com.rits.userservice.model.User;
import com.rits.userservice.model.UserMessageModel;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public UserMessageModel createUser(UserRequest userRequest) throws Exception;

    public UserMessageModel updateUser(UserRequest userRequest) throws Exception;

    public UserMessageModel deleteUser(UserRequest userRequest) throws Exception;

    public User retrieveByUser(UserRequest userRequest) throws Exception;

    public Boolean isUserExists(UserRequest userRequest)  throws Exception;

    public UserListResponse retrieveTop50(UserRequest userRequest) throws Exception;

    public UserListResponse retrieveAllByUser(UserRequest userRequest) throws Exception;

    public AllUserResponse availableUsers(UserRequest userRequest) throws Exception;

    public AvailableUserGroups availableUserGroup(UserRequest userRequest) throws Exception;

    public AvailableWorkCenterList getAllAvailableWorkCenter(UserRequest userRequest) throws Exception;
    public User retrieveByUserOnDefaultSite(UserRequest userRequest) throws Exception;
    public boolean updateDefaultSite(String user, String defaultSite) throws  Exception;

    public Boolean addUserGroup(UserRequest userRequest, List<String> userGroups) throws Exception;

    public Boolean removeUserGroup(UserRequest userRequest, List<String> userGroups) throws Exception;

    User updateUserWithOutUpdatingUserGroup(UserRequest userRequest) throws Exception;
}
