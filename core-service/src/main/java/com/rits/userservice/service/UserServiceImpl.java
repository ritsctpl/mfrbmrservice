package com.rits.userservice.service;

import com.rits.usergroupservice.exception.UserGroupException;
import com.rits.userservice.dto.*;
import com.rits.userservice.exception.UserException;
import com.rits.userservice.model.*;
import com.rits.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Lazy
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${usergroup-service.url}/getAvailableUserGroup")
    private String availableUserGroup;
    @Value("${usergroup-service.url}/assignUser")
    private String assignUser;
    @Value("${usergroup-service.url}/removeUser")
    private String removeUser;
    @Value("${workcenter-service.url}/retrieveBySite")
    private String availableWorkCenter;

    @Value("${user-service.url}/retrieve_detailed_user")
    private String retrieveDetailedUserUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public UserMessageModel createUser(UserRequest userRequest) throws Exception {
        if (isUserExists(userRequest)) {
            throw new UserException(2500, userRequest.getUser());
        }
        if (userRequest.getDefaultSite() == null || userRequest.getDefaultSite().isEmpty()) {
            userRequest.setDefaultSite("*");
        }
//        for (String site : userRequest.getSite()) {
//
//            if (!"RITS".equals(site)) {
//                if (!userRepository.existsByUserAndSiteAndActiveEquals(userRequest.getUser(), "RITS", 1)) {
//                    User defaultUser = userBuilder(userRequest);
//                    defaultUser.setSite(List.of("RITS"));
//                    defaultUser.setHandle("UserBO:RITS," + userRequest.getUser());
//                    defaultUser.setCreatedBy(userRequest.getUserId());
//                    defaultUser.setCreatedDateTime(LocalDateTime.now());
//                    defaultUser.setPassword(userRequest.getPassword());
//                    userRepository.save(defaultUser);
//                }
//            }
//        }
        User createdUser = userBuilder(userRequest);
        createdUser.setHandle("UserBO:" + userRequest.getSite() + "," + userRequest.getUser());
        createdUser.setCreatedBy(userRequest.getUserId());
        createdUser.setCreatedDateTime(LocalDateTime.now());
        createdUser.setPassword(userRequest.getPassword());
        List<com.rits.usergroupservice.model.User> userList = new ArrayList<>();
        com.rits.usergroupservice.model.User userreq = com.rits.usergroupservice.model.User.builder().user(userRequest.getUser()).build();
        userList.add(userreq);
        User user = userRepository.save(createdUser);

        if (userRequest.getUserGroups() != null && !userRequest.getUserGroups().isEmpty()) {
            for (com.rits.userservice.model.UserGroup userGroup : userRequest.getUserGroups()) {
                String userGroupName = userGroup.getUserGroup();

                UserGroupRequests addUserRequest = UserGroupRequests.builder()
                        .site(userRequest.getCurrentSite())
                        .userGroup(userGroupName)
                        .users(userList)
                        .build();

                Boolean addUserResponse = associateUser(addUserRequest);
                if (addUserResponse == null) {
                    throw new UserGroupException(2501, userreq.getUser());
                }
            }
        }
        String createdMessage = getFormattedMessage(1, userRequest.getUser());
        MessageDetails message = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
        return UserMessageModel.builder().message_details(message).response(user).build();
    }

    @Override
    public UserMessageModel updateUser(UserRequest userRequest) throws Exception {
        if (isUserExists(userRequest)) {
            if (userRequest.getDefaultSite() == null || userRequest.getDefaultSite().isEmpty()) {
                userRequest.setDefaultSite("*");
            }

            User existingUser = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            List<com.rits.userservice.model.UserGroup> updatedUserList = userRequest.getUserGroups();
            List<com.rits.userservice.model.UserGroup> existingUserList = existingUser.getUserGroups();
            if (existingUserList != null && !existingUserList.isEmpty()) {

                List<String> userGroupToAdd = new ArrayList<>();
                List<String> userGroupToRemove = new ArrayList();
                for (com.rits.userservice.model.UserGroup updatedUserGroup : updatedUserList) {
                    boolean alreadyExists = existingUserList.stream().anyMatch(user -> user.getUserGroup().equals(updatedUserGroup.getUserGroup()));
                    if (!alreadyExists) {
                        userGroupToAdd.add(updatedUserGroup.getUserGroup());
                    }
                }
                for (com.rits.userservice.model.UserGroup existingUserGroupToremove : existingUserList) {
                    boolean isRemoved = updatedUserList.stream().noneMatch(user -> user.getUserGroup().equals(existingUserGroupToremove.getUserGroup()));
                    if (isRemoved) {
                        userGroupToRemove.add(existingUserGroupToremove.getUserGroup());
                    }
                }
                List<com.rits.usergroupservice.model.User> userList = new ArrayList<>();
                com.rits.usergroupservice.model.User user = new com.rits.usergroupservice.model.User(userRequest.getUser());
                userList.add(user);

                if (!userGroupToAdd.isEmpty()) {
                    for (String userName : userGroupToAdd) {
                        UserGroupRequests addUserRequest = UserGroupRequests.builder()
                                .site(userRequest.getCurrentSite())
                                .userGroup(userName)
                                .users(userList)
                                .build();
                        Boolean addUserResponse = associateUser(addUserRequest);
                        if (addUserResponse == null) {
                            throw new UserGroupException(2501, userRequest.getUser());
                        }
                    }
                }
                List<com.rits.usergroupservice.model.User> userRemovList = new ArrayList<>();
                com.rits.usergroupservice.model.User users = new com.rits.usergroupservice.model.User(userRequest.getUser());
                userRemovList.add(users);
                if (!userGroupToRemove.isEmpty()) {
                    for (String userGroup : userGroupToRemove) {

                        UserGroupRequests removeUserRequest = UserGroupRequests.builder()
                                .site(userRequest.getCurrentSite())
                                .userGroup(userGroup)
                                .users(userRemovList)
                                .build();
                        Boolean removeUserResponse = removeUser(removeUserRequest);
                        if (removeUserResponse == null) {
                            throw new UserGroupException(2501, userRequest.getUser());
                        }
                    }
                }
            } else {
                List<com.rits.usergroupservice.model.User> userList = new ArrayList<>();
                com.rits.usergroupservice.model.User userreq = com.rits.usergroupservice.model.User.builder().user(userRequest.getUser()).build();
                userList.add(userreq);

                if (!userRequest.getUserGroups().isEmpty()) {
                    for (com.rits.userservice.model.UserGroup userGroup : userRequest.getUserGroups()) {
                        String userGroupName = userGroup.getUserGroup();

                        UserGroupRequests addUserRequest = UserGroupRequests.builder()
                                .site(userRequest.getCurrentSite())
                                .userGroup(userGroupName)
                                .users(userList)
                                .build();
                        Boolean addUserResponse = associateUser(addUserRequest);
                        if (addUserResponse == null) {
                            throw new UserGroupException(2501, userreq.getUser());
                        }
                    }
                }

            }
            User updatedUser = userBuilder(userRequest);
            updatedUser.setSite(userRequest.getSite());
            updatedUser.setHandle(existingUser.getHandle());
            updatedUser.setUser(existingUser.getUser());
            updatedUser.setCurrentSite(existingUser.getCurrentSite());
            updatedUser.setHireDate(existingUser.getHireDate());
            updatedUser.setActive(existingUser.getActive());
            updatedUser.setCreatedBy(existingUser.getCreatedBy());
            updatedUser.setCreatedDateTime(existingUser.getCreatedDateTime());
            updatedUser.setModifiedBy(userRequest.getUserId());
            updatedUser.setModifiedDateTime(LocalDateTime.now());
            updatedUser.setPassword(userRequest.getPassword());
            String createdMessage = getFormattedMessage(2, userRequest.getUser());
            MessageDetails message = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
            return UserMessageModel.builder().message_details(message).response(userRepository.save(updatedUser)).build();
        }
        throw new UserException(2501, userRequest.getUser());
    }

    @Override
    public UserMessageModel deleteUser(UserRequest userRequest) throws Exception {
        if (isUserExists(userRequest)) {
            User existingUser = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            existingUser.setActive(0);
            existingUser.setModifiedDateTime(LocalDateTime.now());
            existingUser.setModifiedBy(userRequest.getUserId());
            if (!existingUser.getUserGroups().isEmpty() && existingUser.getUserGroups() != null) {
                List<com.rits.usergroupservice.model.User> userRemovList = new ArrayList<>();
                com.rits.usergroupservice.model.User users = new com.rits.usergroupservice.model.User(userRequest.getUser());
                userRemovList.add(users);
                for (UserGroup userGroup : existingUser.getUserGroups()) {
                    UserGroupRequests removeUserRequest = UserGroupRequests.builder()
                            .site(userRequest.getCurrentSite())
                            .userGroup(userGroup.getUserGroup())
                            .users(userRemovList)
                            .build();
                    Boolean removeUserResponse = removeUser(removeUserRequest);
                }
            }
            userRepository.save(existingUser);
            String createdMessage = getFormattedMessage(3, userRequest.getUser());
            MessageDetails message = MessageDetails.builder().msg(createdMessage).msg_type("S").build();
            return UserMessageModel.builder().message_details(message).build();
        }
        throw new UserException(2501, userRequest.getUser());
    }

    @Override
    public Boolean isUserExists(UserRequest userRequest) throws Exception {
        return userRepository.existsByUserAndActiveEquals(userRequest.getUser(), 1);
//        return userRepository.existsByUserAndSiteAndActiveEquals(userRequest.getUser(),userRequest.getSite().get(0), 1);
    }

    @Override
    public User retrieveByUser(UserRequest userRequest) throws Exception {
        if (isUserExists(userRequest)) {
            return userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
        }
        throw new UserException(2501, userRequest.getUser());
    }

    @Override
    public User retrieveByUserOnDefaultSite(UserRequest userRequest) throws Exception {
//        userRequest.setSite("RITS");//global site i.e., RITS
        List<User> siteList = new ArrayList<>();
        if (isUserExists(userRequest)) {
//            User user = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            List<User> userList = userRepository.findByUserAndActive(userRequest.getUser(), 1);
            if(!userList.isEmpty())
                for(User eachUser : userList){
                    siteList.add(eachUser);
                }
            return siteList.get(0);
        }
        throw new UserException(2501, userRequest.getUser());
    }

//    @Override
//    public boolean updateDefaultSite(String user, String defaultSite) throws Exception {
//
//        boolean flag = false;
//        UserRequest userRequest = UserRequest.builder()
//                .user(user)
//                .currentSite(defaultSite)
//                .build();
//
//        UserResponse response = webClientBuilder.build()
//                .post()
//                .uri(retrieveDetailedUserUrl)
//                .bodyValue(userRequest)
//                .retrieve()
//                .bodyToMono(UserResponse.class)
//                .block();
//
//
//       if(response!=null) {
//           List<User> users = userRepository.findByUserAndActiveEquals(user, 1);
//           if (!users.isEmpty()) {
//               for (User existingUser : users) {
//                   //existingUser.setDefaultSite(defaultSite);
//                   existingUser.setCurrentSite(defaultSite);
//                   existingUser.setModifiedDateTime(LocalDateTime.now());
//                   userRepository.save(existingUser);
//                   flag = true;
//               }
//           }
//           return flag;
//       }
//       return flag;
//    }

    @Override
    public boolean updateDefaultSite(String user, String defaultSite) throws Exception {
        List<User> users = userRepository.findByUserAndActiveEquals(user, 1);
        boolean flag = false;

        UserRequest request = UserRequest.builder()
                .user(user)
                .currentSite(defaultSite)
                .build();

        UserResponse response = webClientBuilder.build()
               .post()
               .uri(retrieveDetailedUserUrl)
               .bodyValue(request)
               .retrieve()
               .bodyToMono(UserResponse.class)
               .block();

        if(response.getUserGroups().size()==0){
            throw new UserException(2508, defaultSite);
        }else {
            if (!users.isEmpty()) {
                for (User existingUser : users) {
                    //existingUser.setDefaultSite(defaultSite);
                    existingUser.setCurrentSite(defaultSite);
                    existingUser.setModifiedDateTime(LocalDateTime.now());
                    userRepository.save(existingUser);
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public UserListResponse retrieveTop50(UserRequest userRequest) throws Exception {
        List<UserResponse> top50Record = userRepository.findTop50ByActiveOrderByCreatedDateTimeDesc(1);
        UserListResponse retrievedTop50record = new UserListResponse(top50Record);
        return retrievedTop50record;
    }

    @Override
    public UserListResponse retrieveAllByUser(UserRequest userRequest) throws Exception {
        if (userRequest.getUser() != null && !userRequest.getUser().isEmpty()) {
            List<UserResponse> retrieveAll = userRepository.findByActiveAndUserContainingIgnoreCase(1, userRequest.getUser());
            UserListResponse allRetrievedRecords = new UserListResponse(retrieveAll);
            return allRetrievedRecords;
        }
        return retrieveTop50(userRequest);
    }

    @Override
    public AllUserResponse availableUsers(UserRequest userRequest) throws Exception
    {
        List<AllUser> allUsers = userRepository.findByActiveEquals(1);
        AllUserResponse allAvailableUsers = new AllUserResponse(allUsers);
        return allAvailableUsers;
    }

    @Override
    public AvailableUserGroups availableUserGroup(UserRequest userRequest) throws Exception
    {
        UserGroupRequest userGroupRequest = new UserGroupRequest(userRequest.getCurrentSite());
        List<AvailableUserGroup> userGroupList = availableUserGroups(userGroupRequest);
        List<String> allUserGroup = new ArrayList<>();
        List<AvailableUserGroup> updatedUserGroup = new ArrayList<>();

        for (int i = 0; i < userGroupList.size(); i++) {
            allUserGroup.add(userGroupList.get(i).getUserGroup());
        }
        if (userRequest.getUser() != null) {
            User existingRecord = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            if (existingRecord != null) {
                List<UserGroup> existingUserGroup = existingRecord.getUserGroups();
                List<String> associatedUserGroup = new ArrayList<>();
                for (int i = 0; i < existingUserGroup.size(); i++) {
                    associatedUserGroup.add(existingUserGroup.get(i).getUserGroup());
                }
                for (int i = 0; i < associatedUserGroup.size(); i++) {
                    if (allUserGroup.contains(associatedUserGroup.get(i))) {// total == aval
                        allUserGroup.remove(associatedUserGroup.get(i));
                    }
                }
//                allUserGroup.retainAll(associatedUserGroup);
            } else
                throw new UserException(2506, userRequest.getUser());
        }
        if (!allUserGroup.isEmpty()) {
            for (int i = 0; i < allUserGroup.size(); i++) {
                AvailableUserGroup userGroup = new AvailableUserGroup(allUserGroup.get(i));
                updatedUserGroup.add(userGroup);
            }
        }
        AvailableUserGroups availableUserGroup = new AvailableUserGroups(updatedUserGroup);
        return availableUserGroup;
    }

    @Override
    public AvailableWorkCenterList getAllAvailableWorkCenter(UserRequest userRequest) {
//        if (userRequest == null || userRequest.getSite() == null) {
//            throw new IllegalArgumentException("Site is required in the request");
//        }
        Set<WorkCenter> allWorkCenters = new HashSet<>();
        Set<WorkCenter> assignedWorkCenters = new HashSet<>();
        Set<WorkCenter> uniqueWorkCenters = new HashSet<>();

        List<User> users = userRepository.findByActive(1);
        if (userRequest.getUser() != null) {
            if (users != null) {
                for (User user : users) {
                    List<WorkCenter> workCenters = user.getWorkCenters();
                    if (workCenters != null) {
                        allWorkCenters.addAll(workCenters);
                    }
                }
            }
            User user = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            if (user == null) {
                return AvailableWorkCenterList.builder().build();
            }
            List<WorkCenter> workCenters = user.getWorkCenters();
            if (workCenters != null) {
                assignedWorkCenters.addAll(workCenters);
            }

            allWorkCenters.removeAll(assignedWorkCenters);
            uniqueWorkCenters.addAll(allWorkCenters);
        } else {
            if (users != null) {
                for (User user : users) {
                    List<WorkCenter> workCenters = user.getWorkCenters();
                    if (workCenters != null) {
                        uniqueWorkCenters.addAll(workCenters);
                    }
                }
            }
        }
        List<AvailableWorkCenter> availableWorkCenterList = uniqueWorkCenters.stream()
                .map(workCenter -> new AvailableWorkCenter(workCenter.getWorkCenter()))
                .collect(Collectors.toList());
        return AvailableWorkCenterList.builder()
                .availableWorkCenterList(availableWorkCenterList)
                .build();
    }

    @Override
    public Boolean addUserGroup(UserRequest userRequest, List<String> userGroups) throws Exception {
        User existingUser = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
//        User existingUser = userRepository.findByActiveAndUser(1,userRequest.getUser());
        if (existingUser != null) {
            List<UserGroup> currentUserGroups = existingUser.getUserGroups();

            for (String group : userGroups) {
                boolean isUserInGroup = currentUserGroups.stream()
                        .anyMatch(userGroup -> userGroup.getUserGroup().equals(group));
                if (!isUserInGroup) {
                    UserGroup newUserGroup = new UserGroup(group);
                    currentUserGroups.add(newUserGroup);
                }
            }
            existingUser.setUserGroups(currentUserGroups);
            userRepository.save(existingUser);
        } else {
            throw new UserException(2501, userRequest.getUser());
        }
        return true;
    }

    @Override
    public Boolean removeUserGroup(UserRequest userRequest, List<String> userGroups) throws Exception {
        User existingUser = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);

        if (existingUser != null) {
            List<UserGroup> currentUserGroups = existingUser.getUserGroups();
            List<UserGroup> groupsToRemove = new ArrayList<>();

            for (String group : userGroups) {
                UserGroup groupToRemove = currentUserGroups.stream()
                        .filter(userGroup -> userGroup.getUserGroup().equals(group))
                        .findFirst()
                        .orElse(null);
                if (groupToRemove != null) {
                    groupsToRemove.add(groupToRemove);
                }
            }
            currentUserGroups.removeAll(groupsToRemove);
            existingUser.setUserGroups(currentUserGroups);
            userRepository.save(existingUser);
        } else {
            throw new UserException(2501, userRequest.getUser());
        }
        return true;
    }

    @Override
    public User updateUserWithOutUpdatingUserGroup(UserRequest userRequest) throws Exception {
        if(isUserExists(userRequest)) {
            if (userRequest.getDefaultSite() == null || userRequest.getDefaultSite().isEmpty()) {
                userRequest.setDefaultSite("*");
            }
            User existingUser = userRepository.findFirstByUserAndActiveEquals(userRequest.getUser(), 1);
            User updatedUser = userBuilder(userRequest);
            updatedUser.setSite(existingUser.getSite());
            updatedUser.setHandle(existingUser.getHandle());
            updatedUser.setUser(existingUser.getUser());
            updatedUser.setHireDate(existingUser.getHireDate());
            updatedUser.setActive(existingUser.getActive());
            updatedUser.setCreatedBy(existingUser.getCreatedBy());
            updatedUser.setCreatedDateTime(existingUser.getCreatedDateTime());
            updatedUser.setModifiedBy(userRequest.getUserId());
            updatedUser.setModifiedDateTime(LocalDateTime.now());
            return userRepository.save(updatedUser);
        }

        throw new UserException(2501,userRequest.getUser());
    }
    public List<AvailableUserGroup> availableUserGroups(UserGroupRequest userGroupRequest){
        List<AvailableUserGroup> userGroupList = webClientBuilder.build()
                .post()
                .uri(availableUserGroup)
                .bodyValue(userGroupRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AvailableUserGroup>>() {
                })
                .block();
        return userGroupList;
    }
    public Boolean associateUser(UserGroupRequests addUserRequest){
        Boolean addUserResponse = webClientBuilder.build()
                .post()
                .uri(assignUser)
                .bodyValue(addUserRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return addUserResponse;
    }
    public Boolean removeUser(UserGroupRequests removeUserRequest){
        Boolean removeUserResponse = webClientBuilder.build()
                .post()
                .uri(removeUser)
                .bodyValue(removeUserRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return removeUserResponse;
    }
    public AvailableWorkCenterList availableWorkCenter(RetrieveRequest retrieveRequest){
        AvailableWorkCenterList workCenterList = webClientBuilder.build()
                .post()
                .uri(availableWorkCenter)
                .bodyValue(retrieveRequest)
                .retrieve()
                .bodyToMono(AvailableWorkCenterList.class)
                .block();
        return workCenterList;
    }
    public User userBuilder(UserRequest userRequest){
//        List<String> processedSites = userRequest.getSite();
//        if (userRequest.getCurrentSite() != null && !processedSites.contains(userRequest.getCurrentSite())) {
//            processedSites.add(userRequest.getCurrentSite());
//        }

        User buildUser = User.builder()
                .site(userRequest.getSite())
                .handle(userRequest.getHandle())
                .defaultSite("*")
                .currentSite(userRequest.getCurrentSite())
                .user(userRequest.getUser())
                .lastName(userRequest.getLastName())
                .firstName(userRequest.getFirstName())
                .emailAddress(userRequest.getEmailAddress())
                .status(userRequest.getStatus())
                .employeeNumber(userRequest.getEmployeeNumber())
                .hireDate(userRequest.getHireDate())
                .erpUser(userRequest.getErpUser())
                .erpPersonnelNumber(userRequest.getErpPersonnelNumber())
                .userGroups(userRequest.getUserGroups())
                .workCenters(userRequest.getWorkCenters())
                .labourTracking(userRequest.getLabourTracking())
                .supervisor(userRequest.getSupervisor())
                .labourRules(userRequest.getLabourRules())
                .customDataList(userRequest.getCustomDataList())
                .active(1)
                .build();
        if(userRequest.getUserGroups() == null)
        {
            List<UserGroup> newUserGroups = new ArrayList<>();
            buildUser.setUserGroups(newUserGroups);
        }
        return buildUser;
    }

    public List<String> getSiteListByUser(User user){
        List<String> siteList = new ArrayList<>();
        if(!user.getUser().isEmpty()){
            List<User> userList = userRepository.findByUserAndActive(user.getUser(), 1);
            if(!userList.isEmpty())
                for(User eachUser : userList){
                    siteList.addAll(eachUser.getSite());
                }
        }

        return siteList;
    }
}