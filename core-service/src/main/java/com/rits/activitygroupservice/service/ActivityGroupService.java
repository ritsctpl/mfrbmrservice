package com.rits.activitygroupservice.service;
import com.rits.activitygroupservice.dto.*;
import com.rits.activitygroupservice.model.ActivityGroup;
import com.rits.activitygroupservice.model.ActivityGroupMessageModel;
import com.rits.usergroupservice.dto.UserGroupRequest;
import com.rits.usergroupservice.model.Activity;

import java.util.List;

public interface ActivityGroupService {
    public ActivityGroupMessageModel createActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception;
    public ActivityGroupResponseList getActivityGroupListByCreationDate(ActivityGroupRequest activityGroupRequest) throws Exception;
    public ActivityGroupResponseList getActivityGroupList(ActivityGroupRequest activityGroupName) throws Exception;
    public ActivityGroup retrieveActivityGroup(ActivityGroupRequest activityGroupName) throws Exception;
    public ActivityGroupMessageModel updateActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception;
    public ActivityGroupMessageModel deleteActivityGroup(ActivityGroupRequest activityGroupRequest) throws Exception;
    public Boolean associateActivityMemberToActivityGroupMemberList(AddRemoveActivityRequest activityGroupMemberListRequest) throws Exception;
    public Boolean removeActivityMemberFromActivityGroupMemberList(AddRemoveActivityRequest activityGroupMemberListRequest) throws Exception;
    public ActivityGroupMemberResponseList getActivityGroupMemberList(ActivityGroupMemberListRequest activityGroupMemberListRequest) throws Exception;

    public AvailableActivityList getAvailableActivities(ActivityGroupRequest activityGroupRequest) throws Exception;

    public Boolean isActivityGroupExist(ActivityGroupRequest activityGroupRequest) throws Exception;

    List<ActivityGroup> retrieveAllBySite();

    String callExtension(Extension extension);

    public List<ActivityGroupResponse> getActivities(UserGroupRequest userGroupRequest) throws Exception;

}
