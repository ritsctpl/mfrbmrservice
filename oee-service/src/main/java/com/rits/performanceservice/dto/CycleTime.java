package com.rits.performanceservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_CYCLE_TIME")
public class CycleTime {
    @Id
    private String handle;
    private String site;
    private String shopOrder;
    private String routing;
    private String routingVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String item;
    private String itemVersion;
    private double cycleTime;
    private double manufacturedTime;
    private Integer active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
