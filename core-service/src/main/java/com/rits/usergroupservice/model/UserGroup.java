package com.rits.usergroupservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_USERGROUP")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserGroup {
    private String site;
    @Id
    private String handle;
    private String userGroup;
    private String description;
    private String pod;
    private List<User> users;
    private List<ActivityGroup> permissionForActivityGroup;
    private List<CustomData> customDataList;
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;


}
