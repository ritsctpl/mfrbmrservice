package com.rits.cycletimeservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "RESOURCE_TYPE")
public class ResourceType {
    @Id
    private String handle;
    private String site;
    private String resourceType;
    private String resourceTypeDescription;
    private List<ResourceMemberList> resourceMemberList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userID;
}
