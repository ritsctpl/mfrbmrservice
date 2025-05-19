package com.rits.logbuyoffservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BuyOff {
    private String site;
    private String buyOff;
    private String version;
    private String handle;
    private String description;
    private String status;
    private String messageType;
    private boolean partialAllowed;
    private boolean rejectAllowed;
    private boolean skipAllowed;
    private boolean currentVersion;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
