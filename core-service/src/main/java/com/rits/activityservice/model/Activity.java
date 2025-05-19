package com.rits.activityservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_ACTIVITY")
public class Activity {
    @Id
    private String handle;
    private String site;
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
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String imageUrl;
}
