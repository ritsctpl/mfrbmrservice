package com.rits.activitygroupservice.dto;

import com.rits.activityservice.model.ActivityName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ActivityRequest {
    private String site;
    private String activityId;
    private List<ActivityName> activityGroupList;
}
