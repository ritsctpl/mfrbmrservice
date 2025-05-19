package com.rits.extensionservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Activity {
    private String handle;
    private String site;
    private String activityId;
    private String description;
    private String activityGroup;
    private String url;
    private Boolean enabled;
    private Boolean visibleInActivityManager;
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
