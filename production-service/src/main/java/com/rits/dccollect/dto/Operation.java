package com.rits.dccollect.dto;

import com.rits.checkhook.dto.ActivityHook;
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
@Document(collection = "R_OPERATION")
public class Operation {
    @Id
    private String handle;
    private String site;
    private String operation;
    private String revision;
    private String description;
    private String status;
    private String operationType;
    private String resourceType;
    private String defaultResource;
    private String erpOperation;
    private boolean addAsErpOperation;
    private List<ActivityHook> activityHookList;
    private String workCenter;
    private boolean currentVersion;
    private int maxLoopCount;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private List<Certification> certificationList;

}

