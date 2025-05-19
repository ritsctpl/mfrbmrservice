package com.rits.usergroupservice.model;

import com.rits.activitygroupservice.model.ActivityGroupMember;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ActivityGroup {
    private String activityGroupName;
    private String activityGroupDescription;
    private List<Activity> permissionForActivity;
    private boolean enabled;
    private List<ActivityGroupMember> activityGroupMemberList;

    public ActivityGroup(String activityGroupName){
        this.activityGroupName = activityGroupName;
    }
}
