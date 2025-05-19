package com.rits.activityhookservice.dto;

import com.rits.activityhookservice.models.AttachmentList;
import lombok.*;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityHookRequest {
    private String site;
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
    private String userId;
    private Integer active;
}