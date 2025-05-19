package com.rits.productionlogservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTime {
    @Id
    private String handle;
    private String site;
    private String operationId;
    private String operationVersion;
    private String resourceId;
    private String resourceType;
    private String item;
    private String itemVersion;
    private String material;
    private String materialVersion;
    private String workCenterId;
    private Double cycleTime;
    private Double manufacturedTime;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private Integer active;
    private String pcu;
    private String shiftId;
    private String userId;
    private Double targetQuantity;
    private String time;
}
