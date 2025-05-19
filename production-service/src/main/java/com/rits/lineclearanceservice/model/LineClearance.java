package com.rits.lineclearanceservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "R_LINE_CLEARANCE_TEMPLATE")
public class LineClearance {
    @Id
    private String handle;
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

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}






