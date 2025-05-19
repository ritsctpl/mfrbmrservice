package com.rits.reasoncodeservice.dto;

import com.rits.reasoncodeservice.model.CustomData;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReasonCodeRequest {
    private String site;
    private String reasonCode;
    private String description;
    private String category;
    private String status;
    private String messageType;
    private List<CustomData> customDataList;
    private Integer active;
    private String userId;
    private List<String> workCenter;
    private List<String> resource;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
