package com.rits.resourcetypeservice.dto;

import com.rits.resourcetypeservice.Model.ResourceMemberList;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResourceTypeRequest {
    private String handle;
    private String site;
    private String resourceType;
    private String resourceTypeDescription;
    private List<ResourceMemberList> resourceMemberList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
