package com.rits.activityservice.dto;

import com.rits.activityservice.model.ActivityHook;
import com.rits.activityservice.model.ActivityName;
import com.rits.activityservice.model.ActivityRule;
import com.rits.activityservice.model.ListOfMethods;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ActivityResponse {
    private String handle;
    //private String site;
    private String currentSite;
    private String activityId;
    private String description;
    private List<ActivityName> activityGroupList;
    private String url;
    private boolean enabled;
    private boolean visibleInActivityManager;
    private String type;
    private List<ListOfMethods> listOfMethods;
    private List<ActivityRule> activityRules;
    private List<ActivityHook> activityHookList;
    private int active;
    private String createdBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String imageUrl;
}
