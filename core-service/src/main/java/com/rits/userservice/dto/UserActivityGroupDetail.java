package com.rits.userservice.dto;

import com.rits.activityservice.model.Activity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActivityGroupDetail {
    private String activityGroup;
    private String activityGroupDescription;
    private boolean enabled;
    private List<Activity> activityList=new ArrayList<>();
}
