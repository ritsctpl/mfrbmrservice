package com.rits.buyoffservice.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "R_BUYOFF")
@Builder
public class BuyOff {
    private String site;
    private String buyOff;
    private String version;
    @Id
    private String handle;
    private String description;
    private String status;
    private String messageType;
    private boolean partialAllowed;
    private boolean rejectAllowed;
    private boolean skipAllowed;
    private boolean currentVersion;
    private List<String> tags;
    private List<UserGroupList> userGroupList;
    private List<AttachmentList> attachmentList;
    private List<CustomDataList> customDataList;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
