package com.rits.buyoffservice.dto;

import com.rits.buyoffservice.model.AttachmentList;
import com.rits.buyoffservice.model.CustomDataList;
import com.rits.buyoffservice.model.UserGroupList;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BuyOffResponse {
    private String site;
    private String buyOff;
    private String version;
    @Id
    private String handle;
    private String description;
    private String status;
    private String messageType;
    private String partialAllowed;
    private String rejectAllowed;
    private String skipAllowed;
    private String currentVersion;
    private List<UserGroupList> userGroupList;
    private List<AttachmentList> attachmentList;
    private List<CustomDataList> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
