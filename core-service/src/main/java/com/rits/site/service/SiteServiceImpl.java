package com.rits.site.service;

import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.site.dto.*;
import com.rits.site.exception.SiteException;
import com.rits.site.model.ActivityHookList;
import com.rits.site.model.MessageDetails;
import com.rits.site.model.MessageModel;
import com.rits.site.model.Site;
import com.rits.site.repository.SiteRepository;
import com.rits.usergroupservice.controller.UserGroupController;
import com.rits.usergroupservice.dto.SiteInfo;
import com.rits.usergroupservice.model.ActivityGroup;
import com.rits.usergroupservice.dto.UserGroupRequest;
import com.rits.usergroupservice.model.UserGroupMessageModel;
import com.rits.usergroupservice.repository.UserGroupRepository;
import com.rits.usergroupservice.service.UserGroupServiceImpl;
import com.rits.userservice.controller.UserServiceController;
import com.rits.userservice.dto.UserRequest;
import com.rits.userservice.model.User;
import com.rits.usergroupservice.model.UserGroup;
import com.rits.userservice.model.UserMessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Lazy
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{
    private final SiteRepository siteRepository;
    private final UserGroupServiceImpl userGroupService;
    private final UserGroupRepository userGroupRepository;

    @Lazy
    private final UserServiceController userServiceController;
    @Lazy
    private final UserGroupController userGroupController;

    private final WebClient.Builder webClientBuilder;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${activity-service.url}/isExist")
    private String isActivityExistUrl;

    @Value("${usergroup-service.url}/userGroupCopyForNewSite")
    private String userGroupCopyForNewSite;

//    {
//
//        "site":"RITS2",
//
//            "description":"rits2",
//
//            "type":"type3",
//
//            "timeZone":[],
//
//        "local":true
//
//    }

    @Override
    public MessageModel createSite(SiteRequest siteRequest) throws Exception
    {
        if(isSiteExists(siteRequest))
            throw new SiteException(2100,siteRequest.getSite());

        getValidated(siteRequest);

        if(siteRequest.getDescription() == null || siteRequest.getDescription().isEmpty())
            siteRequest.setDescription(siteRequest.getSite());

        Site site = Site.builder()
                .site(siteRequest.getSite())
                .handle("SiteBO:"+siteRequest.getSite())
                .description(siteRequest.getDescription())
                .type(siteRequest.getType())
                .timeZone(siteRequest.getTimeZone())
                .activityHookLists(siteRequest.getActivityHookLists())
                .theme(siteRequest.getTheme())
                .createdBy(siteRequest.getUserId())
                .local(siteRequest.isLocal())
                .createdDateTime(LocalDateTime.now())
                .userId(siteRequest.getUserId())
                .build();

        Site savedSite = siteRepository.save(site);
        createUserGroupCopyForNewSite(siteRequest);
        List<Site> allSites = siteRepository.findAll();

        MessageModel messageModel = MessageModel.builder()
                .message_details(new MessageDetails(siteRequest.getSite()+" Created SuccessFully","S"))
                .response(savedSite)
                .allSites(allSites)
                .build();


       /* if (retriveGlobalSiteResponse != null) {
            List<String> userGroups = retriveGlobalSiteResponse.getUserGroups();
            List<ActivityGroup> activityGroups = retriveGlobalSiteResponse.getPermissionForActivityGroup();

            if (userGroups != null) {
                for (String userGroupName : userGroups) {
                    UserGroup newUserGroup = UserGroup.builder()
                            .site(savedSite.getSite())
                            .userGroup(userGroupName)
                            .description(userGroupName + " Description")
                            .active(1)
                            .createdDateTime(LocalDateTime.now())
                            .build();
                    List<ActivityGroup> permissionForActivityGroup = new ArrayList<>();
                    if (activityGroups != null) {
                        for (ActivityGroup activityGroup : activityGroups) {
                            ActivityGroup newActivityGroup = new ActivityGroup(activityGroup.getActivityGroupName());
                            permissionForActivityGroup.add(newActivityGroup);
                        }
                    }
                    newUserGroup.setPermissionForActivityGroup(userGroupName.);

                    userGroupRepository.save(newUserGroup);
                }
            }
        }*/

        if(messageModel.getResponse()!=null && messageModel.getResponse().getSite()!=null && !messageModel.getResponse().getSite().isEmpty())
        {

            UserRequest userRequest = UserRequest.builder().user("rits_sys").build();
            ResponseEntity<Boolean> userExistsResponse = userServiceController.isUserExists(userRequest);
            Boolean userExists = userExistsResponse.getBody();
            if(Boolean.FALSE.equals(userExists)) {
                UserRequest createUserRequest = UserRequest.builder()
                        .site(Collections.singletonList(siteRequest.getSite()))
                        .defaultSite("*")
                        .user("rits_sys")
                        .lastName("Sys")
                        .firstName("Rits")
                        .status("Active")
                        .build();
                ResponseEntity<UserMessageModel> createUser = userServiceController.createUser(createUserRequest);
            }

             List<com.rits.usergroupservice.model.User> users=new ArrayList<>();
            com.rits.usergroupservice.model.User user = com.rits.usergroupservice.model.User.builder().user("rits_sys").build();
             users.add(user);

             List<ActivityGroup> permissionForActivityGroup = new ArrayList<>();
             ActivityGroup activityGroup = new ActivityGroup("COMMON");
             permissionForActivityGroup.add(activityGroup);

            UserGroupRequest userGroupRequest = new UserGroupRequest();
            userGroupRequest.setSite(siteRequest.getSite());
            userGroupRequest.setUserGroup("SYSTEM_ADMINISTRATOR");

            UserGroupRequest createUserGroupRequest = null;
            if(!userGroupController.isExist(userGroupRequest)) {
                createUserGroupRequest = UserGroupRequest.builder()
                        .site(siteRequest.getSite())
                        .userGroup("SYSTEM_ADMINISTRATOR")
                        .users(users)
                        .permissionForActivityGroup(permissionForActivityGroup)
                        .description("System Administrator")
                        .build();
                ResponseEntity<UserGroupMessageModel> userGroupMessage = userGroupController.createUserGroup(createUserGroupRequest);
            }

             List<com.rits.userservice.model.UserGroup> userGroups = new ArrayList<>();
            com.rits.userservice.model.UserGroup userGroup = com.rits.userservice.model.UserGroup.builder().userGroup("SYSTEM_ADMINISTRATOR").build();
             userGroups.add(userGroup);
            UserRequest updateUserRequest = UserRequest.builder()
                    .currentSite(siteRequest.getSite())
                    .site(Collections.singletonList(siteRequest.getSite()))  //kartheek nasina
                    .defaultSite("*")
                    .user("rits_sys")
                    .userGroups(userGroups)
                    .lastName("Sys")
                    .firstName("Rits")
                    .status("Active")
                    .build();
            ResponseEntity<User> updateUser = userServiceController.updateUserWithOutUpdatingUserGroup(updateUserRequest);

            UserGroupRequest userGroupRequest1=UserGroupRequest.builder().site("*").userGroup("SYSTEM_ADMINISTRATOR").build();
            if(!userGroupController.isExist(userGroupRequest1)) {
                createUserGroupRequest = UserGroupRequest.builder()
                        .site("*")
                        .userGroup("SYSTEM_ADMINISTRATOR")
                        .users(users)
                        .permissionForActivityGroup(permissionForActivityGroup)
                        .description("System Administrator")
                        .build();
                userGroupController.createUserGroup(createUserGroupRequest);
            }
        }
        return messageModel;
    }

    public void createUserGroupCopyForNewSite(SiteRequest siteRequest){
        SiteRequest siteRequests= SiteRequest.builder()
                .site(siteRequest.getSite())
                .build();

        Boolean created = webClientBuilder
                .build()
                .post()
                .uri(userGroupCopyForNewSite)
                .body(BodyInserters.fromValue(siteRequests))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }


//    {
//        "site":"RITS"
//    }

    @Override
    public Boolean isSiteExists(SiteRequest siteRequest) throws Exception
    {
        return siteRepository.existsBySite(siteRequest.getSite());
    }

//    {
//
//        "site":"RITS2",
//
//            "description":"rits2",
//
//            "type":"type3",
//
//            "timeZone":[],
//
//        "local":false
//
//    }

    @Override
    public MessageModel updateSite(SiteRequest siteRequest) throws Exception {
        if (!isSiteExists(siteRequest)) {
            throw new SiteException(2101, siteRequest.getSite());
        }
        getValidated(siteRequest);

        if (siteRequest.getDescription() == null || siteRequest.getDescription().isEmpty()) {
            siteRequest.setDescription(siteRequest.getSite());
        }

        // Retrieve the existing site
        Site existingSite = siteRepository.findBySite(siteRequest.getSite());
        // Build the updated site, using existing createdDateTime
        Site updatedSite = Site.builder()
                .site(siteRequest.getSite())
                .handle(existingSite.getHandle())
                .description(siteRequest.getDescription())
                .type(siteRequest.getType())
                .activityHookLists(siteRequest.getActivityHookLists())
                .theme(siteRequest.getTheme())
                .local(existingSite.isLocal())
                .timeZone(siteRequest.getTimeZone())
                .modifiedBy(siteRequest.getUserId())
                .createdDateTime(existingSite.getCreatedDateTime()) // Use the existing createdDateTime
                .createdBy(existingSite.getCreatedBy()) // Use the existing createdBy if necessary
                .updatedDateTime(LocalDateTime.now())
                .userId((siteRequest.getUserId()))
                .build();

        List<Site> allSites = siteRepository.findAll();

        // Save the updated site to the repository
        return MessageModel.builder()
                .message_details(new MessageDetails(siteRequest.getSite() + " Updated Successfully", "S"))
                .response(siteRepository.save(updatedSite))
                .allSites(allSites)
                .build();

    }


    private void getValidated(SiteRequest siteRequest) throws Exception {
        isActivityValid(siteRequest);
    }

    private void isActivityValid(SiteRequest siteRequest) throws Exception {
        if (siteRequest.getActivityHookLists() != null && !siteRequest.getActivityHookLists().isEmpty()) {
            for (ActivityHookList activityHook : siteRequest.getActivityHookLists()) {
                if (!activityExist(activityHook.getActivity(), siteRequest.getSite())) {
                    throw new SiteException(2406, activityHook.getActivity());
                }
            }
        }
    }

    private boolean activityExist(String activity, String site) {
        ActivityHookRequest isExist = ActivityHookRequest.builder().activityId(activity).site(site).build();
        Boolean isActivityExist = webClientBuilder.build()
                .post()
                .uri(isActivityExistUrl) // URL for checking activity existence
                .bodyValue(isExist)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return isActivityExist;
    }


    @Override
    public TimeZoneResponse retrieveTimeZoneList() throws Exception
    {

//            Site existingSite = siteRepository.findBySite(siteRequest.getSite());
            Set<String> zoneIds = ZoneId.getAvailableZoneIds();
            List<String> timeZones = new ArrayList<>(zoneIds);
            List<TimeZone> timeZoneList = new ArrayList<TimeZone>();
            for(int i=0;i<timeZones.size();i++)
            {
                TimeZone timeZone = new TimeZone(timeZones.get(i));
                timeZoneList.add(timeZone);
            }
           TimeZoneResponse timeZoneResponse = new TimeZoneResponse(timeZoneList);
            return timeZoneResponse;

    }

//    nothing to pass

    @Override
    public RetrieveTop50Response retrieveTop50() throws Exception
    {
        List<RetrieveTop50> top50List = siteRepository.findTop50ByOrderByCreatedDateTimeDesc();
        RetrieveTop50Response top50Response = new RetrieveTop50Response(top50List);
        return top50Response;
    }

//    {
//        "site":"RITS1"
//    }

    @Override
    public Site retrieveBySite(SiteRequest siteRequest) throws Exception
    {
        if(isSiteExists(siteRequest))
        {
//            return siteRepository.findBySite(siteRequest.getSite());
            Site site = siteRepository.findBySite(siteRequest.getSite());
            site.setTheme(site.getTheme() == null ? new Theme("", "", "", "") : site.getTheme());
            return site;
        }
        throw new SiteException(2101,siteRequest.getSite());
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
            throw new SiteException(800);
        }
        return extensionResponse;
    }

    @Override
    public AuditLogRequest createAuditLog(SiteRequest siteRequest) {
       return AuditLogRequest.builder()
                .site(siteRequest.getSite())
                .action_code("SITE-CREATED ")
                .action_detail("Site Created")
                .action_detail_handle("ActionDetailBO:" + siteRequest.getSite() + "," + "SITE-CREATED " + siteRequest.getUserId() + ":" + "com.rits.site.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(siteRequest.getUserId())
                .txnId("SITE-CREATED" + String.valueOf(LocalDateTime.now()) + siteRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Created")
                .topic("audit-log")
                .build();
    }

    @Override
    public AuditLogRequest updateAuditLog(SiteRequest siteRequest) {
        return AuditLogRequest.builder()
                .site(siteRequest.getSite())
                .action_code("SITE-UPDATED")
                .action_detail("Site Updated")
                .action_detail_handle("ActionDetailBO:" + siteRequest.getSite() + "," + "SITE-UPDATED " + siteRequest.getUserId() + ":" + "com.rits.site.controller")
                .activity("From Service")
                .date_time(String.valueOf(LocalDateTime.now()))
                .userId(siteRequest.getUserId())
                .txnId("SITE-UPDATED" + String.valueOf(LocalDateTime.now()) + siteRequest.getUserId())
                .created_date_time(String.valueOf(LocalDateTime.now()))
                .category("Update")
                .topic("audit-log")
                .build();
    }
}
