package com.rits.activityservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.activitygroupservice.dto.AddRemoveActivityRequest;
import com.rits.activitygroupservice.exception.ActivityGroupException;
import com.rits.activityservice.dto.*;


import com.rits.activityservice.exception.ActivityException;
import com.rits.activityservice.model.Activity;
import com.rits.activityservice.model.ActivityName;
import com.rits.activityservice.model.MessageDetails;
import com.rits.activityservice.model.ActivityMessageModel;
import com.rits.activityservice.repository.ActivityRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Data
@RequiredArgsConstructor
@Service
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;

    @Value("${activitygroup-service.url}/isExist")
    private String activityGroupServiceUrl;

    @Value("${activitygroup-service.url}/add")
    private String associateActivityMemberToActivityGroupMemberList;

    @Value("${activitygroup-service.url}/remove")
    private String removeActivityMemberFromActivityGroupMemberList;



    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public ActivityMessageModel createActivity(ActivityRequest activityRequest) throws Exception {
        long recordPresent = activityRepository.countByActivityIdAndActive(activityRequest.getActivityId(), 1); //db.getCollection("activity").countDocuments({ activityId: "",active: true})
        if (recordPresent > 0) {
            throw new ActivityException(1300, activityRequest.getActivityId());
        } else {
            for (ActivityName activityGroupName : activityRequest.getActivityGroupList()) {
                ActivityGroupRequest activityGroupRequest = ActivityGroupRequest.builder().activityGroupName(activityGroupName.getActivityGroupName()).build();
                Boolean activityGroupExists = isActivityGroupExist(activityGroupRequest);
                if (!activityGroupExists) {
                    throw new ActivityException(1304, activityGroupName.getActivityGroupName());
                }
            }
            if (activityRequest.getDescription() == null || activityRequest.getDescription().isEmpty()) {
                activityRequest.setDescription(activityRequest.getActivityId());
            }
            if (activityRequest.getActivityGroupList() != null && !activityRequest.getActivityGroupList().isEmpty()) {
                List<String> activityGroupList = activityRequest.getActivityGroupList().stream().map(ActivityName::getActivityGroupName).collect(Collectors.toList());
                AddRemoveActivityRequest activityNameReq = AddRemoveActivityRequest.builder()
                        .activityId(activityRequest.getActivityId())
                        .activityGroupName(activityGroupList)
                        .build();
                Boolean activityResponseEntity = associateActivity(activityNameReq);
            }
            Activity activity = activityBuilder(activityRequest);
            activity.setHandle("ActivityBO:" + activityRequest.getCurrentSite() + "," + activityRequest.getActivityId());
            activity.setCreatedBy(activityRequest.getUserId());
            activity.setCreatedDateTime(LocalDateTime.now());
            String createdMessage = getFormattedMessage(1, activityRequest.getActivityId());
            return ActivityMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(activityRepository.save(activity)).build();
        }
    }

    @Override
    public ActivityMessageModel updateActivity(ActivityRequest activityRequest) throws Exception {
        Activity activity = activityRepository.findByActivityIdAndActive(activityRequest.getActivityId(), 1);
        if (activity != null) {
            if (activityRequest.getDescription() == null || activityRequest.getDescription().isEmpty()) {
                activityRequest.setDescription(activityRequest.getActivityId());
            }
            List<ActivityName> updatedActivityMember = activityRequest.getActivityGroupList();
            List<ActivityName> existingGroupMember = activity.getActivityGroupList();
            if (existingGroupMember != null && !existingGroupMember.isEmpty()) {
                List<String> activityNameToAdd = new ArrayList<>();
                List<String> activityNameToRemove = new ArrayList<>();
                for (ActivityName activityMember : updatedActivityMember) {
                    boolean alreadyExists = existingGroupMember.stream().anyMatch(member -> member.getActivityGroupName().equals(activityMember.getActivityGroupName()));
                    if (!alreadyExists) {
                        activityNameToAdd.add(activityMember.getActivityGroupName());
                    }
                }

                for (ActivityName existingMember : existingGroupMember) {
                    boolean isRemoved = updatedActivityMember.stream().noneMatch(member -> member.getActivityGroupName().equals(existingMember.getActivityGroupName()));
                    if (isRemoved) {
                        activityNameToRemove.add(existingMember.getActivityGroupName());
                    }
                }

                List<String> activityList = new ArrayList<>();
                activityList.add(activityRequest.getActivityId());


                if (!activityNameToAdd.isEmpty()) {
                    List<String> activityGroupList = activityRequest.getActivityGroupList().stream().map(ActivityName::getActivityGroupName).collect(Collectors.toList());
                    AddRemoveActivityRequest activityNameReq = AddRemoveActivityRequest.builder()
                            .activityId(activityRequest.getActivityId())
                            .activityGroupName(activityGroupList)
                            .build();

                    Boolean activityResponseEntity = associateActivity(activityNameReq);

                    if (activityResponseEntity == null || !activityResponseEntity) {
                        throw new ActivityGroupException(1301, activityRequest.getActivityId());
                    }
                }

                if (!activityNameToRemove.isEmpty()) {
                    AddRemoveActivityRequest activityNamRequest = AddRemoveActivityRequest.builder()
                            .activityGroupName(activityNameToRemove)
                            .activityId(activityRequest.getActivityId())
                            .build();
                    Boolean activityResponseEntity = removeActivity(activityNamRequest);
                    if (activityResponseEntity == null || !activityResponseEntity) {
                        throw new ActivityGroupException(1301, activityRequest.getActivityId());
                    }
                }
            } else {
                List<String> activityGroupList = new ArrayList<>();
                activityGroupList.add(activityRequest.getActivityId());

                if (!activityRequest.getActivityId().isEmpty()) {
                    AddRemoveActivityRequest activityNameReq = AddRemoveActivityRequest.builder()
                            .activityId(activityRequest.getActivityId())
                            .activityGroupName(activityGroupList)
                            .build();
                    Boolean activityResponseEntity = associateActivity(activityNameReq);
                    if (activityResponseEntity == null || !activityResponseEntity) {
                        throw new ActivityGroupException(1301, activityRequest.getActivityId());
                    }
                }
            }
            Activity updateActivity = activityBuilder(activityRequest);
            updateActivity.setHandle(activity.getHandle());
            //updateActivity.setSite(activity.getSite());
            updateActivity.setCurrentSite(activity.getCurrentSite());
            updateActivity.setCreatedBy(activity.getCreatedBy());
            updateActivity.setCreatedDateTime(activity.getCreatedDateTime());
            updateActivity.setActivityId(activity.getActivityId());
            updateActivity.setModifiedBy(activityRequest.getUserId());
            updateActivity.setModifiedDateTime(LocalDateTime.now());
            String createdMessage = getFormattedMessage(2, activityRequest.getActivityId());
            return ActivityMessageModel.builder().message_details(new MessageDetails(createdMessage, "S")).response(activityRepository.save(updateActivity)).build();
        } else {
            throw new ActivityException(1301, activityRequest.getActivityId());
        }
    }

    @Override
    public Boolean addActivityGroupName(ActivityRequest activityRequest) throws Exception {
        Activity existingActivity = activityRepository.findByActivityIdAndActive(activityRequest.getActivityId(), 1);

        if (existingActivity == null) {
            throw new ActivityException(1301, activityRequest.getActivityId());
        }
        String newActivityGroupName = activityRequest.getActivityGroupList().get(0).getActivityGroupName();
        boolean nameExists = existingActivity.getActivityGroupList().stream()
                .anyMatch(activityName -> activityName.getActivityGroupName().equals(newActivityGroupName));

        if (!nameExists) {
            ActivityName newActivityName = new ActivityName();
            newActivityName.setActivityGroupName(newActivityGroupName);
            existingActivity.getActivityGroupList().add(newActivityName);
            existingActivity.setModifiedDateTime(LocalDateTime.now());
        }
        activityRepository.save(existingActivity);
        return true;
    }


    @Override
    public ActivityListResponseList getActivityListByCreationDate(ActivityRequest activityRequest) throws Exception {
        List<ActivityListResponse> activityResponses = activityRepository.findTop50ByActiveOrderByCreatedDateTimeDesc(1);
        return ActivityListResponseList.builder().activityList(activityResponses).build();
    }


    @Override
    public ActivityListResponseList getActivityList(ActivityRequest activityRequest) throws Exception {

        if (activityRequest.getActivityId() == null || activityRequest.getActivityId().isEmpty()) {
            return getActivityListByCreationDate(activityRequest);
        } else {
            List<ActivityListResponse> activityResponses = activityRepository.findByActivityIdContainingIgnoreCaseAndActive(activityRequest.getActivityId(), 1);//db.getCollection("activity").find({ activityIde: { $regex: "substring", $options: "i" } ,active:true})
            if (activityResponses != null && !activityResponses.isEmpty()) {
                return ActivityListResponseList.builder().activityList(activityResponses).build();
            } else {
                throw new ActivityException(1301, activityRequest.getActivityId());
                // return ActivityListResponseList.builder().activityList(new ArrayList<>()).build();
            }
        }
    }


    @Override
    public ActivityHookResponseList getActivityHookList(ActivityHookListRequest activityHookListRequest) throws Exception {
        List<Activity> hookableActivities = activityRepository.findByActivityHookList_EnableAndActivityIdAndActive(true, activityHookListRequest.getActivityId(), 1);
        if (hookableActivities == null || hookableActivities.isEmpty()) {
            throw new ActivityException(1301, activityHookListRequest.getActivityId());
        }
        List<ActivityHookResponse> activityHookResponses = hookableActivities.stream()
                .flatMap(activity -> activity.getActivityHookList().stream())
                .filter(activityHook -> activityHook.isEnable())
                .map(activityHook -> ActivityHookResponse.builder()
                        .sequence(activityHook.getSequence())
                        .hookPoint(activityHook.getHookPoint())
                        .activity(activityHook.getActivity())
                        .hookableMethod(activityHook.getHookableMethod())
                        .build())
                .collect(Collectors.toList());
        if (activityHookResponses.isEmpty()) {
            throw new ActivityException(1303, activityHookListRequest.getActivityId());
        }
        return ActivityHookResponseList.builder().activityHookList(activityHookResponses).build();
    }

    @Override
    public Activity retrieveActivity(ActivityRequest activityRequest) throws Exception {
        try {
            Activity existingActivity = activityRepository.findByActivityIdAndActive(activityRequest.getActivityId(), 1);
            if (existingActivity == null) {
                throw new ActivityException(1301, activityRequest.getActivityId());
            }
            // Use an iterator to safely remove elements during iteration.
            Iterator<ActivityName> iterator = existingActivity.getActivityGroupList().iterator();
            while (iterator.hasNext()) {
                ActivityName activityGroupName = iterator.next();
                ActivityGroupRequest activityGroupRequest = ActivityGroupRequest.builder()
                        .activityGroupName(activityGroupName.getActivityGroupName())
                        .build();
                Boolean activityGroupExists = isActivityGroupExist(activityGroupRequest);
                if (!activityGroupExists) {
                    iterator.remove();
                }
            }
            return existingActivity;
        } catch (Exception ex) {
            // Log exception details for debugging.
            System.err.println("Error in retrieveActivity: " + ex.getMessage());
            // Rethrow the exception to be handled by the caller.
            throw ex;
        }
    }

    @Override
    public List<ActivityResponse> getAllActivities(ListOfActivityRequest listOfActivityRequest) throws Exception {
        List<ActivityResponse> activities = new ArrayList<>();
        List<Activity> activityList = activityRepository.findByActive(1);
        if (activityList != null && !activityList.isEmpty()) {
            List<ActivityResponse> activityResponseList = activityList.stream()
                    .map(activity -> ActivityResponse.builder()
                            .handle(activity.getHandle())
                            //.site(activity.getSite())
                            .activityId(activity.getActivityId())
                            .description(activity.getDescription())
                            .activityGroupList(activity.getActivityGroupList())
                            .url(activity.getUrl())
                            .enabled(activity.isEnabled())
                            .visibleInActivityManager(activity.isVisibleInActivityManager())
                            .type(activity.getType())
                            .listOfMethods(activity.getListOfMethods())
                            .activityRules(activity.getActivityRules())
                            .activityHookList(activity.getActivityHookList())
                            .active(activity.getActive())
                            .createdBy(activity.getCreatedBy())
                            .createdDateTime(activity.getCreatedDateTime())
                            .modifiedDateTime(activity.getModifiedDateTime())
                            .imageUrl(activity.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
            activities.addAll(activityResponseList);
        }

        return activities;
    }

//    @Override
//    public List<ActivityResponse> getServiceActivities(ListOfActivityRequest listOfActivityRequest) throws Exception {
//        List<ActivityResponse> activities = new ArrayList<>();
//        List<Activity> activityList = activityRepository.findByActiveAndType(1, "Service");
//        if (activityList != null && !activityList.isEmpty()) {
//            List<ActivityResponse> activityResponseList = activityList.stream()
//                    .map(activity -> ActivityResponse.builder()
//                            .handle(activity.getHandle())
//                            //.site(activity.getSite())
//                            .activityId(activity.getActivityId())
//                            .description(activity.getDescription())
//                            .activityGroupList(activity.getActivityGroupList())
//                            .url(activity.getUrl())
//                            .enabled(activity.isEnabled())
//                            .visibleInActivityManager(activity.isVisibleInActivityManager())
//                            .type(activity.getType())
//                            .listOfMethods(activity.getListOfMethods())
//                            .activityRules(activity.getActivityRules())
//                            .activityHookList(activity.getActivityHookList())
//                            .active(activity.getActive())
//                            .createdBy(activity.getCreatedBy())
//                            .createdDateTime(activity.getCreatedDateTime())
//                            .modifiedDateTime(activity.getModifiedDateTime())
//                            .imageUrl(activity.getImageUrl())
//                            .build())
//                    .collect(Collectors.toList());
//            activities.addAll(activityResponseList);
//
//        }
//        return activities;
//    }

    @Override
    public List<ActivityResponse> getServiceActivities(ListOfActivityRequest listOfActivityRequest) throws Exception {
        List<Activity> activityList;

        if (listOfActivityRequest.getActivity() == null || listOfActivityRequest.getActivity().isEmpty()) {
            activityList = activityRepository.findTop50ByTypeAndActiveOrderByCreatedDateTimeDesc(listOfActivityRequest.getType(),1);
        } else {
            activityList = activityRepository.findByActivityIdContainingIgnoreCaseAndTypeAndActive(
                    listOfActivityRequest.getActivity(), listOfActivityRequest.getType(), 1);
        }

        return activityList.stream()
                .map(activity -> ActivityResponse.builder()
                        .handle(activity.getHandle())
                        .activityId(activity.getActivityId())
                        .description(activity.getDescription())
                        .activityGroupList(activity.getActivityGroupList())
                        .url(activity.getUrl())
                        .enabled(activity.isEnabled())
                        .visibleInActivityManager(activity.isVisibleInActivityManager())
                        .type(activity.getType())
                        .listOfMethods(activity.getListOfMethods())
                        .activityRules(activity.getActivityRules())
                        .activityHookList(activity.getActivityHookList())
                        .active(activity.getActive())
                        .createdBy(activity.getCreatedBy())
                        .createdDateTime(activity.getCreatedDateTime())
                        .modifiedDateTime(activity.getModifiedDateTime())
                        .imageUrl(activity.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }






    @Override
    public List<ActivityHookListResponse> getHookActivities(ListOfActivityRequest listOfActivityRequest) throws Exception {
       List<ActivityHookListResponse> activityResponseList= activityRepository.findByTypeAndActive("Hooks", 1);
       return activityResponseList;
    }



    @Override
    public List<ActivityResponse> getListOfActivityByActivityGroup(List<String> activityGroup) throws Exception {
        List<ActivityResponse> activities = new ArrayList<>();

        for (String group : activityGroup) {
            List<Activity> activityList = activityRepository.findByActivityGroupListActivityGroupName(group);
            if (activityList != null && !activityList.isEmpty()) {
                List<ActivityResponse> activityResponseList = activityList.stream()
                        .map(activity -> ActivityResponse.builder()
                                .handle(activity.getHandle())
                                //.site(activity.getSite())
                                //.currentSite((activity.getCurrentSite()))
                                .activityId(activity.getActivityId())
                                .description(activity.getDescription())
                                .activityGroupList(activity.getActivityGroupList())
                                .url(activity.getUrl())
                                .enabled(activity.isEnabled())
                                .visibleInActivityManager(activity.isVisibleInActivityManager())
                                .type(activity.getType())
                                .listOfMethods(activity.getListOfMethods())
                                .activityRules(activity.getActivityRules())
                                .activityHookList(activity.getActivityHookList())
                                .active(activity.getActive())
                                .createdBy(activity.getCreatedBy())
                                .createdDateTime(activity.getCreatedDateTime())
                                .modifiedDateTime(activity.getModifiedDateTime())
                                .imageUrl(activity.getImageUrl())
                                .build())
                        .collect(Collectors.toList());
                activities.addAll(activityResponseList);
                return activities;
            }
        }
        throw new ActivityException(1304, activityGroup);
    }

    @Override
    public Response deleteActivity(ActivityRequest activityRequest) throws Exception {
        if (activityRepository.existsByActivityIdAndActive(activityRequest.getActivityId(), 1)) {
            Activity existingActivity = activityRepository.findByActivityId(activityRequest.getActivityId());
            existingActivity.setActive(0);
            existingActivity.setModifiedDateTime(LocalDateTime.now());
            existingActivity.setModifiedBy(activityRequest.getUserId());
            if(!existingActivity.getActivityGroupList().isEmpty() && existingActivity.getActivityGroupList() != null) {
                List<String> activityGroups = existingActivity.getActivityGroupList().stream().map(ActivityName::getActivityGroupName).collect(Collectors.toList());
                AddRemoveActivityRequest activityNamRequest = AddRemoveActivityRequest.builder()
                        .activityGroupName(activityGroups)
                        .activityId(existingActivity.getActivityId())
                        .build();
                removeActivity(activityNamRequest);
            }
            activityRepository.save(existingActivity);
            String createdMessage = getFormattedMessage(3, activityRequest.getActivityId());
            Response response = Response.builder().message(createdMessage).build();
            return response;
        } else {
            throw new ActivityException(1301, activityRequest.getActivityId());
        }
    }

    @Override
    public Boolean isActivityExist(ActivityRequest activityRequest) throws Exception {
        return activityRepository.existsByActivityIdAndActive(activityRequest.getActivityId(), 1);
    }

    @Override
    public Boolean removeActivityGroupMemberFromActivity(String activityGroupName, List<String> activityIds) {
        Boolean flag = false;
        for (String activity : activityIds) {
            Activity existingActivity = activityRepository.findByActivityIdAndActive(activity, 1);
            if (existingActivity == null || existingActivity.getActivityId() == null) {
                throw new ActivityException(1301, activity);
            }
            List<ActivityName> activityGroupList = existingActivity.getActivityGroupList();
            activityGroupList.removeIf(member -> member.getActivityGroupName().equals(activityGroupName));
            existingActivity.setActivityGroupList(activityGroupList);
            activityRepository.save(existingActivity);
            flag = true;
        }
        return flag;
    }

    @Override
    public String getActivityUrl(String activityId) throws Exception {
        Activity activity= activityRepository.findByActivityIdAndActive(activityId,1);
        if(activity!= null && activity.getActivityId()!=null){
            return activity.getUrl();
        }else{
            throw new ActivityException(1301,activityId);
        }
    }

    public Boolean isActivityGroupExist(ActivityGroupRequest activityGroupRequest) {
        Boolean activityGroupExists = webClientBuilder.build()
                .post()
                .uri(activityGroupServiceUrl)
                .bodyValue(activityGroupRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return activityGroupExists;
    }

    public Boolean associateActivity(AddRemoveActivityRequest activityNameReq) {
        Boolean activityResponseEntity = webClientBuilder
                .build()
                .post()
                .uri(associateActivityMemberToActivityGroupMemberList)
                .bodyValue(activityNameReq)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return activityResponseEntity;
    }

    public Boolean removeActivity(AddRemoveActivityRequest activityNamRequest) {
        Boolean activityResponseEntity = webClientBuilder
                .build()
                .post()
                .uri(removeActivityMemberFromActivityGroupMemberList)
                .bodyValue(activityNamRequest)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return activityResponseEntity;
    }


    public Activity activityBuilder(ActivityRequest activityRequest) {
        Activity activity = Activity.builder()
                .activityId(activityRequest.getActivityId())
                .activityGroupList(activityRequest.getActivityGroupList())
                .site("*")
                .currentSite(activityRequest.getCurrentSite())
                .description(activityRequest.getDescription())
                .url(activityRequest.getUrl())
                .enabled(activityRequest.isEnabled())
                .visibleInActivityManager(activityRequest.isVisibleInActivityManager())
                .type(activityRequest.getType())
                .activityRules(activityRequest.getActivityRules())
                .activityHookList(activityRequest.getActivityHookList())
                .active(1)
                .imageUrl(activityRequest.getImageUrl())
                .build();
        return activity;
    }

    @Override
    public Boolean readJSONData(String jsonData) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        if (jsonData.trim().startsWith("[")) {
            List<Activity> dataList = mapper.readValue(jsonData, new TypeReference<List<Activity>>() {});
            activityRepository.saveAll(dataList);  // Save all data using MongoRepository
            System.out.println("Data saved to MongoDB (multiple entries).");
        } else {
            // Handle single JSON object
            Activity data = mapper.readValue(jsonData, Activity.class);  // Convert to Activity
            activityRepository.save(data);
            // Save single data
            System.out.println("Data saved to MongoDB (single entry).");
        }
        return true;
    }

    @Override
    public Activity getByActivityId(String site, String activityId) {
        Activity activity = activityRepository.findBySiteAndActivityIdAndActive(site, activityId, 1);
        return activity;
    }
}

