package com.rits.activityservice.service;

import com.rits.activityservice.dto.*;
import com.rits.activityservice.model.Activity;
import com.rits.activityservice.model.ActivityMessageModel;

import java.util.List;
public interface ActivityService {
    public ActivityMessageModel createActivity(ActivityRequest activityRequest) throws Exception;

    public Boolean addActivityGroupName(ActivityRequest activityRequest) throws Exception;

    public ActivityListResponseList getActivityListByCreationDate(ActivityRequest activityRequest) throws Exception;
    public ActivityListResponseList getActivityList(ActivityRequest activityRequest) throws Exception;
    public ActivityHookResponseList getActivityHookList(ActivityHookListRequest activityHookListRequest) throws Exception;

//    public  ActivityHookEnabledResponseList getEnabledActivityHook(ActivityRequest activityRequest) ;

    public Activity retrieveActivity(ActivityRequest activityRequest) throws Exception;

    public List<ActivityResponse> getAllActivities( ListOfActivityRequest listOfActivityRequest) throws Exception;

    List<ActivityResponse> getServiceActivities(ListOfActivityRequest listOfActivityRequest) throws Exception;

    List<ActivityHookListResponse> getHookActivities(ListOfActivityRequest listOfActivityRequest) throws Exception;

    public List<ActivityResponse> getListOfActivityByActivityGroup(List<String> activityGroup) throws Exception;

    public ActivityMessageModel updateActivity(ActivityRequest activityRequest) throws Exception;
    public Response deleteActivity(ActivityRequest activityRequest) throws Exception;

    Boolean isActivityExist(ActivityRequest activityRequest) throws Exception;

    public Boolean removeActivityGroupMemberFromActivity(String activityGroupName, List<String> activityIds);
    String getActivityUrl(String activityId) throws Exception;
    Boolean readJSONData(String jsonData) throws Exception;

    Activity getByActivityId(String site, String activityId);
}
