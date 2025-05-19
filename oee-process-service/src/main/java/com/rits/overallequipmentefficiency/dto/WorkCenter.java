package com.rits.overallequipmentefficiency.dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore fields not explicitly declared
public class WorkCenter {
    private String site;
    private String handle;
    private String workCenter;
    private String description;
    private String status;
    private String routing;
    private String routingVersion;
    private String workCenterCategory;
    private String erpWorkCenter;
    private String defaultParentWorkCenter;
    private boolean addAsErpWorkCenter;
    private List<Association> associationList;
    private List<CustomData> customDataList;
    private List<ActivityHook> activityHookList;
    private boolean inUse;
    private boolean trackOee; // New field
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;

}
