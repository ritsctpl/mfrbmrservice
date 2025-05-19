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
public class BuyOffRequest {
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
    private List<UserGroupList> userGroupList;
    private List<AttachmentList> attachmentList;
    private List<CustomDataList> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;

    private String batchNo;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
}
