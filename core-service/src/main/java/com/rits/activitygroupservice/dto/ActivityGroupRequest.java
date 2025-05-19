package com.rits.activitygroupservice.dto;

import com.rits.activitygroupservice.model.ActivityGroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ActivityGroupRequest {
    //private String site;
    private String currentSite;
    private String activityGroupName;
    private String activityGroupDescription;
    private List<String> activityId;
    private List<ActivityGroupMember> activityGroupMemberList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;

    public ActivityGroupRequest(String currentSite, String activityGroupName) {
        //this.site=site;
        this.currentSite=currentSite;
        this.activityGroupName=activityGroupName;
    }
}
