package com.rits.operationservice.dto;
import com.rits.resourcetypeservice.Model.ResourceMemberList;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PodResponse {
    private String podName;
    private String description;
    private String status;
    private String defaultResource;
    private String site;
    private String resourceType;
    private String resourceTypeDescription;
    private List<ResourceMemberList> resourceMemberList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userID;
}

