package com.rits.reasoncodeservice.model;

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
@Document(collection = "R_REASON_CODE")
public class ReasonCode {
    @Id
    private String handle;
    private String site;
    private String reasonCode;
    private String description;
    private String category;
    private String status;
    private String messageType;
    private List<CustomData> customDataList;
    private Integer active;
    private List<String> workCenter;
    private List<String> resource;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
