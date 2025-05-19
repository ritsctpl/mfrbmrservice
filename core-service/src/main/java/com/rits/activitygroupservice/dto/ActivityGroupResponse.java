package com.rits.activitygroupservice.dto;

import com.rits.activitygroupservice.model.ActivityGroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityGroupResponse {
    private String activityGroupName;
    private String activityGroupDescription;
    private List<ActivityGroupMember> activityIds;
}
