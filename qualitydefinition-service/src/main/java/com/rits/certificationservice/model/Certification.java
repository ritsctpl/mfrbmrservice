package com.rits.certificationservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_CERTIFICATE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Certification {

    private String site;
    private String certification;
    @Id
    private String handle;
    private String description;
    private String duration;
    private String durationType;
    private String status;
    private String maxNumberOfExtensions;
    private String maxExtensionDuration;
    private List<UserGroup> userGroupList;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
