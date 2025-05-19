package com.rits.activitygroupservice.service;

import com.rits.activitygroupservice.dto.*;
import com.rits.activitygroupservice.exception.*;
import com.rits.activitygroupservice.model.ActivityGroup;
import com.rits.activitygroupservice.model.ActivityGroupMember;
import com.rits.activitygroupservice.model.MessageDetails;
import com.rits.activitygroupservice.model.ActivityGroupMessageModel;
import com.rits.activitygroupservice.repository.ActivityGroupRepository;
import com.rits.activityservice.controller.ActivityController;
import com.rits.activityservice.dto.ListOfActivityRequest;

import com.rits.activityservice.model.ActivityName;
import com.rits.usergroupservice.dto.UserGroupRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Service
@RequiredArgsConstructor
public class ActivityGroupServiceImpl implements ActivityGroupService {
    private final ActivityGroupRepository activityGroupRepository;
    private final ActivityController activityController;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${activity-service.url}/retrieveAll")
    private String activityServiceUrl;

    @Value("${extension-service.url}/addExtension")
    private String extensionUrl;

    @Value("${activity-service.url}/addActivityGroupName")
    private String addActivityGroupName;

    @Value("${activity-service.url}/removeActivityGroupMember")
    private String removeActivityGroupMemberFromActivity;

    @Value("${activity-service.url}/isExist")
    private String activityExistUrl;

    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ActivityGroupMessageModel createActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception{
        long recordPresent = activityGroupRepository.countByActivityGroupNameAndActive(activityGroupRequest.getActivityGroupName(), 1); //db.getCollection("activity-group").countDocuments({ activityGroupName: "",active: true})
        if (recordPresent > 0) {
            throw new ActivityGroupException(1400, activityGroupRequest.getActivityGroupName());
        }
        if(activityGroupRequest.getActivityGroupDescription()==null || activityGroupRequest.getActivityGroupDescription().isEmpty()){
            activityGroupRequest.setActivityGroupDescription(activityGroupRequest.getActivityGroupName());
        }
        ActivityGroup activityGroup = ActivityGroup.builder()
                .handle("ActivityGroupBO:" + activityGroupRequest.getCurrentSite() + "," + activityGroupRequest.getActivityGroupName())
                .activityGroupDescription(activityGroupRequest.getActivityGroupDescription())
                .activityGroupName(activityGroupRequest.getActivityGroupName())
                .activityGroupMemberList(activityGroupRequest.getActivityGroupMemberList())
                //.site(activityGroupRequest.getSite())
                //.currentSite((activityGroupRequest.getCurrentSite()))
                .createdBy(activityGroupRequest.getUserId())
                .createdDateTime(LocalDateTime.now())
                .active(1)
                .build();
        String createdMessage = getFormattedMessage(1, activityGroupRequest.getActivityGroupName());
        return ActivityGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(activityGroupRepository.save(activityGroup)).build();
    }

    @Override
    public ActivityGroupResponseList getActivityGroupListByCreationDate(ActivityGroupRequest activityGroupRequest) throws Exception{
        List<ActivityGroupResponse> activityGroupResponses = activityGroupRepository.findTop50ByActiveOrderByCreatedDateTimeDesc(1);
        return ActivityGroupResponseList.builder().activityGroupList(activityGroupResponses).build();
    }

    @Override
    public ActivityGroupResponseList getActivityGroupList(ActivityGroupRequest activityGroupRequest) throws Exception{

        if (activityGroupRequest.getActivityGroupName() == null || activityGroupRequest.getActivityGroupName().isEmpty()) {
            return getActivityGroupListByCreationDate(activityGroupRequest);
        } else {
            List<ActivityGroupResponse> activityGroupResponses = activityGroupRepository.findByActivityGroupNameContainingIgnoreCaseAndActive(activityGroupRequest.getActivityGroupName(), 1);//db.getCollection("activity-group").find({ activityGroupName: { $regex: "substring", $options: "i" } ,active:true})
            if (activityGroupResponses != null && !activityGroupResponses.isEmpty()) {
                return ActivityGroupResponseList.builder().activityGroupList(activityGroupResponses).build();
            } else {
                throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
            }
        }
    }

    @Override
    public ActivityGroup retrieveActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception{
        ActivityGroup activityGroup =activityGroupRepository.findByActiveAndActivityGroupName(  1,activityGroupRequest.getActivityGroupName());
        if (activityGroup != null) {
            return activityGroup;
        } else {
            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
        }
    }

    @Override
    public ActivityGroupMessageModel updateActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception{
        ActivityGroup activityGroup = activityGroupRepository.findByActiveAndActivityGroupName(  1,activityGroupRequest.getActivityGroupName());
        if (activityGroup != null) {
            if(activityGroupRequest.getActivityGroupDescription()==null || activityGroupRequest.getActivityGroupDescription().isEmpty())
            {
                activityGroupRequest.setActivityGroupDescription(activityGroupRequest.getActivityGroupName());
            }

            List<ActivityGroupMember> updatedActivityMemberList = activityGroupRequest.getActivityGroupMemberList();
            List<ActivityGroupMember> existingGroupMemberList = activityGroup.getActivityGroupMemberList();

            if(existingGroupMemberList != null && !existingGroupMemberList.isEmpty()) {
                List<String> activityGroupMemberToAdd = new ArrayList<>();
                List<String> activityGroupMemberToRemove = new ArrayList<>();

                for (ActivityGroupMember activityMember : updatedActivityMemberList) {
                    boolean alreadyExists = existingGroupMemberList.stream().anyMatch(member -> member.getActivityId().equals(activityMember.getActivityId()));
                    if (!alreadyExists) {
                        activityGroupMemberToAdd.add(activityMember.getActivityId());
                    }
                }

                for (ActivityGroupMember existingMember : existingGroupMemberList) {
                    boolean isRemoved = updatedActivityMemberList.stream().noneMatch(member -> member.getActivityId().equals(existingMember.getActivityId()));
                    if (isRemoved) {
                        activityGroupMemberToRemove.add(existingMember.getActivityId());
                    }
                }

                List<ActivityName> activityGroupList = new ArrayList<>();
                ActivityName activityGroupReq = new ActivityName(activityGroupRequest.getActivityGroupName());
                activityGroupList.add(activityGroupReq);


                if (!activityGroupMemberToAdd.isEmpty()) {
                    for (String activity : activityGroupMemberToAdd) {
                        ActivityRequest activityRequest= ActivityRequest.builder()
                                //.site(activityGroupRequest.getSite())
                                .activityId(activity)
                                .activityGroupList(activityGroupList)
                                .build();

                        Boolean activityResponseEntity = associateActivityGroup(activityRequest);
                        if (activityResponseEntity == null || !activityResponseEntity) {
                            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
                        }
                    }
                }

                List<String> activityGroupRemovList = new ArrayList<>();
                activityGroupRemovList.add(activityGroupRequest.getActivityGroupName());

                if (!activityGroupMemberToRemove.isEmpty()) {
                  ListOfActivityRequest activityRequest = ListOfActivityRequest.builder()
                            //.site(activityGroupRequest.getSite())
                            //.currentSite((activityGroupRequest.getCurrentSite()))
                            .activityGroup(activityGroupRemovList)
                            .activityId(activityGroupMemberToRemove)
                            .build();

                    Boolean activityResponseEntity = removeActivityGroup(activityRequest);

                    if (activityResponseEntity == null || !activityResponseEntity) {
                        throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
                    }
                }
            }
            else{
                List<ActivityName> activityGroupList = new ArrayList<>();
                ActivityName activityGroupReq = new ActivityName(activityGroupRequest.getActivityGroupName());
                activityGroupList.add(activityGroupReq);

                if (!activityGroupRequest.getActivityGroupMemberList().isEmpty()) {
                    for (ActivityGroupMember activity : activityGroupRequest.getActivityGroupMemberList()) {
                       ActivityRequest activityRequest = ActivityRequest.builder()
                                //.site(activityGroupRequest.getSite())
                                .activityId(activity.getActivityId())
                                .activityGroupList(activityGroupList)
                                .build();

                        Boolean activityResponseEntity = associateActivityGroup(activityRequest);
                        if (activityResponseEntity == null || !activityResponseEntity) {
                            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
                        }
                    }
                }
            }

            ActivityGroup updatedActivityGroup = ActivityGroup.builder()
                    .handle(activityGroup.getHandle())
                    //.site(activityGroup.getSite())
                    //.currentSite(activityGroup.getCurrentSite())
                    .activityGroupName(activityGroup.getActivityGroupName())
                    .activityGroupDescription(activityGroupRequest.getActivityGroupDescription())
                    .activityGroupMemberList(activityGroupRequest.getActivityGroupMemberList())
                    .createdDateTime(activityGroup.getCreatedDateTime())
                    .modifiedBy(activityGroupRequest.getUserId())
                    .modifiedDateTime(LocalDateTime.now())
                    .active(1)
                    .build();
            String createdMessage = getFormattedMessage(2, activityGroupRequest.getActivityGroupName());
            return ActivityGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).response(activityGroupRepository.save(updatedActivityGroup)).build();
        } else {
            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
        }
    }

    @Override
    public ActivityGroupMessageModel deleteActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception{
        if (activityGroupRepository.existsByActivityGroupNameAndActive(activityGroupRequest.getActivityGroupName(), 1)) {
            ActivityGroup existingActivityGroup = activityGroupRepository.findByActiveAndActivityGroupName(  1,activityGroupRequest.getActivityGroupName());
            existingActivityGroup.setActive(0);
            existingActivityGroup.setModifiedDateTime(LocalDateTime.now());
            existingActivityGroup.setModifiedBy(activityGroupRequest.getUserId());
            if(!existingActivityGroup.getActivityGroupMemberList().isEmpty() && existingActivityGroup.getActivityGroupMemberList() !=null) {
                List<String> activityGroupRemovList = new ArrayList<>();
                activityGroupRemovList.add(existingActivityGroup.getActivityGroupName());
                List<String> activityGroupMemberToRemove = existingActivityGroup.getActivityGroupMemberList().stream().map(ActivityGroupMember::getActivityId).collect(Collectors.toList());
                ListOfActivityRequest activityRequest = ListOfActivityRequest.builder()
                        //.site(activityGroupRequest.getSite())
                        //.currentSite(activityGroupRequest.getCurrentSite())
                        .activityGroup(activityGroupRemovList)
                        .activityId(activityGroupMemberToRemove)
                        .build();
                removeActivityGroup(activityRequest);
            }
            activityGroupRepository.save(existingActivityGroup);
            String createdMessage = getFormattedMessage(3, activityGroupRequest.getActivityGroupName());
            return ActivityGroupMessageModel.builder().message_details(new MessageDetails(createdMessage,"S")).build();
        } else {
            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());

        }
    }

    @Override
    public Boolean associateActivityMemberToActivityGroupMemberList(AddRemoveActivityRequest activityGroupMemberListRequest) throws Exception{
        if(activityGroupMemberListRequest.getActivityGroupName() != null && !activityGroupMemberListRequest.getActivityGroupName().isEmpty()){
            for(String activityGroup : activityGroupMemberListRequest.getActivityGroupName())
            {
                ActivityGroup retrievedActivityGroup = activityGroupRepository.findByActiveAndActivityGroupName(1,activityGroup);
                if (retrievedActivityGroup != null && retrievedActivityGroup.getActivityGroupMemberList()!=null && !retrievedActivityGroup.getActivityGroupMemberList().isEmpty()) {
                   Boolean isActivityAssociated = retrievedActivityGroup.getActivityGroupMemberList().stream().anyMatch(activity -> activity.getActivityId().equals(activityGroupMemberListRequest.getActivityId()));
                   if(!isActivityAssociated)
                   {
                       ActivityGroupMember newActivity = ActivityGroupMember.builder().activityId(activityGroupMemberListRequest.getActivityId()).build();
                       retrievedActivityGroup.getActivityGroupMemberList().add(newActivity);
                       activityGroupRepository.save(retrievedActivityGroup);
                   }
                }
            }
        }
        return true;
    }


    @Override
    public Boolean removeActivityMemberFromActivityGroupMemberList(AddRemoveActivityRequest activityGroupMemberListRequest) throws Exception{
        Boolean isRemoved = false;
        if(activityGroupMemberListRequest.getActivityGroupName() != null && !activityGroupMemberListRequest.getActivityGroupName().isEmpty()){
            for(String activityGroup : activityGroupMemberListRequest.getActivityGroupName())
            {
                ActivityGroup retrievedActivityGroup = activityGroupRepository.findByActiveAndActivityGroupName( 1,activityGroup);
                if (activityGroup != null && retrievedActivityGroup.getActivityGroupMemberList()!=null && !retrievedActivityGroup.getActivityGroupMemberList().isEmpty()) {
                    Boolean isActivityPresent = retrievedActivityGroup.getActivityGroupMemberList().stream().anyMatch(activity -> activity.getActivityId().equals(activityGroupMemberListRequest.getActivityId()));
                    if(isActivityPresent)
                    {
                        retrievedActivityGroup.getActivityGroupMemberList().removeIf(activity -> activity.getActivityId().equals(activityGroupMemberListRequest.getActivityId()));
                        isRemoved = true;
                        activityGroupRepository.save(retrievedActivityGroup);
                    }
                }
            }
        }
        return isRemoved;
    }
    @Override
    public ActivityGroupMemberResponseList getActivityGroupMemberList(ActivityGroupMemberListRequest activityGroupMemberListRequest) throws Exception {
        ActivityGroup activityGroup = activityGroupRepository.findByActiveAndActivityGroupName(  1,activityGroupMemberListRequest.getActivityGroupName());
       ActivityGroupMemberResponseList activityGroupMemberResponseList = new ActivityGroupMemberResponseList();
        List<ActivityGroupMemberResponse> activityGroupMemberResponses = new ArrayList<>();
        if (activityGroup != null) {
            for(ActivityGroupMember activity : activityGroup.getActivityGroupMemberList())
            {
                ActivityRequest activityRequest = ActivityRequest.builder().site(activityGroupMemberListRequest.getSite()).activityId(activity.getActivityId()).build();
                Boolean availableActivities = isActivityExist(activityRequest);
                if(availableActivities)
                {
                    ActivityGroupMemberResponse activityGroupMemberResponse = ActivityGroupMemberResponse.builder().activityId(activity.getActivityId()).build();
                    activityGroupMemberResponses.add(activityGroupMemberResponse);
                    activityGroupMemberResponseList.setActivityGroupMemberList(activityGroupMemberResponses);
                }
            }
            return activityGroupMemberResponseList;
            }
        throw new ActivityGroupException(1401, activityGroupMemberListRequest.getActivityGroupName());
        }
    @Override
    public AvailableActivityList getAvailableActivities(ActivityGroupRequest activityGroupRequest) throws Exception{
        if( activityGroupRequest.getActivityGroupName()!=null && !activityGroupRequest.getActivityGroupName().isEmpty() ) {
            List<ActivityGroup> activityGroups = activityGroupRepository.findByActivityGroupNameAndActive(activityGroupRequest.getActivityGroupName(), 1);
            if (activityGroups != null && !activityGroups.isEmpty()) {
                List<String> activityIds = new ArrayList<>();
                for (ActivityGroup activityGroup : activityGroups) {
                    List<ActivityGroupMember> activityGroupMembers = activityGroup.getActivityGroupMemberList();
                    if (activityGroupMembers != null && !activityGroupMembers.isEmpty()) {
                        activityIds.addAll(activityGroupMembers.stream()
                                .map(ActivityGroupMember::getActivityId)
                                .collect(Collectors.toList()));
                    }
                }
                ListOfActivityRequest listOfActivityRequest = new ListOfActivityRequest();
//                listOfActivityRequest.setSite(activityGroupRequest.getSite());

                List<ActivityResponse> availableActivities = availableActivites(listOfActivityRequest);
                if (availableActivities != null) {
                    availableActivities.removeIf(activity -> activityIds.contains(activity.getActivityId()));
                }
                return AvailableActivityList.builder().availableActivitiesList(availableActivities).build();
            }
            throw new ActivityGroupException(1401, activityGroupRequest.getActivityGroupName());
        }
            ListOfActivityRequest listOfActivityRequest = new ListOfActivityRequest();
//            listOfActivityRequest.setSite(activityGroupRequest.getSite());

            List<ActivityResponse> availableActivities =availableActivites(listOfActivityRequest);
            return AvailableActivityList.builder().availableActivitiesList(availableActivities).build();
        }

    @Override
    public Boolean isActivityGroupExist(ActivityGroupRequest activityGroupRequest) throws Exception{
        return activityGroupRepository.existsByActivityGroupNameAndActive(activityGroupRequest.getActivityGroupName(),1);
    }

    @Override
    public List<ActivityGroup> retrieveAllBySite(){
       List<ActivityGroup> allActivityGroups=activityGroupRepository.findByActive(1);
       return allActivityGroups;
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
        if (extensionResponse==null) {
            throw new ActivityGroupException(800);
        }
        return extensionResponse;
    }
    public  List<ActivityResponse> availableActivites(ListOfActivityRequest listOfActivityRequest){
        List<ActivityResponse> availableActivities = webClientBuilder
                .build()
                .post()
                .uri(activityServiceUrl)
                .body(BodyInserters.fromValue(listOfActivityRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ActivityResponse>>() {
                })
                .block();
        return availableActivities;
    }
    public Boolean associateActivityGroup(ActivityRequest activityRequest){
        Boolean activityResponseEntity = webClientBuilder
                .build()
                .post()
                .uri(addActivityGroupName)
                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return activityResponseEntity;
    }
    public Boolean removeActivityGroup(ListOfActivityRequest activityRequest){
        Boolean activityResponseEntity = webClientBuilder
                .build()
                .post()
                .uri(removeActivityGroupMemberFromActivity)
                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return activityResponseEntity;
    }
    public Boolean isActivityExist(ActivityRequest activityRequest)
    {
        Boolean availableActivities = webClientBuilder
                .build()
                .post()
                .uri(activityExistUrl)
                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return availableActivities;
    }

    @Override
    public List<ActivityGroupResponse> getActivities(UserGroupRequest userGroupRequest) throws Exception {

        List<ActivityGroup> activityGroups = activityGroupRepository.findByActive(1);

        if (activityGroups == null)
            throw new IllegalArgumentException("no record found");

        List<ActivityGroupResponse> activityGroupList = new ArrayList<>();
        ActivityGroupResponse activityGroupObj;

        for(ActivityGroup activityGroup : activityGroups){
            activityGroupObj = new ActivityGroupResponse();
            activityGroupObj.setActivityGroupName(activityGroup.getActivityGroupName());
            activityGroupObj.setActivityGroupDescription(activityGroup.getActivityGroupDescription());
            activityGroupObj.setActivityIds(activityGroup.getActivityGroupMemberList());
            activityGroupList.add(activityGroupObj);
        }

        return activityGroupList;
    }
}




