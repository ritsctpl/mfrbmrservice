package com.rits.activityhookservice.models;

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
@Document(collection = "R_ACTIVITY_HOOK")
public class ActivityHook {
    @Id
    private String activityHookId;
    private String description;
    private String targetClass;
    private String targetMethod;
    private String hookType;
    private String hookPoint;
    private String hookClass;
    private String hookMethod;
    private String executionMode;
    private List<AttachmentList> attachmentList;
    private String site;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
    private String userId;
    private Integer active;
}