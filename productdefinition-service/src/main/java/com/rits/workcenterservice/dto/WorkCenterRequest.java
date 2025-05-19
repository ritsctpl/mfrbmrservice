package com.rits.workcenterservice.dto;

import com.rits.workcenterservice.model.ActivityHook;
import com.rits.workcenterservice.model.Association;
import com.rits.workcenterservice.model.CustomData;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WorkCenterRequest {
    private String site;
    private String workCenter;
    private String description;
    private String status;
    private String routing;
    private String routingVersion;
    private String workCenterCategory;
    private String erpWorkCenter;
    private String defaultParentWorkCenter;
    private boolean addAsErpWorkCenter;
    private boolean trackOee; // New field
    private List<Association> associationList;
    private List<CustomData> customDataList;
    private List<ActivityHook> activityHookList;
    private boolean inUse;


}
