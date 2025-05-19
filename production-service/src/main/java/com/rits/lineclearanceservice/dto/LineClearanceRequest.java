package com.rits.lineclearanceservice.dto;

import com.rits.lineclearanceservice.model.AssociatedLocation;
import com.rits.lineclearanceservice.model.Task;
import com.rits.lineclearanceservice.model.UserRole;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LineClearanceRequest {
    private String site;
    private String templateName;
    private String description;
    private String clearanceTimeLimit;
    private Boolean notifyOnCompletion;
    private Integer maxPendingTasks;
    private String clearanceReminderInterval;
    private Boolean enablePhotoEvidence;
    private String userId;
    private List<AssociatedLocation> associatedTo;
    private List<Task> tasks;
    private List<UserRole> userRoles;
    private Integer active;
}
