package com.rits.cycletimeservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeResponse {
    private String operation;
    private String operationVersion;
    private String resource;
    private String resourceType;
    private String material;
    private String materialVersion;
    private String itemId;
    private String item;
    private String itemVersion;
    private String workCenter;
    private double cycleTime;
    private double manufacturedTime;
    private String userId;
    private String handle;
    private String time;
    private Integer active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private Double targetQuantity;

    private List<CycleTimeResponse> cycleTimeResponseList;
    //private String time;
}
