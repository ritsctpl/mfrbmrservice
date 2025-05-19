package com.rits.performance.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogRequest {
    private String site;
    private String eventId;
    private String eventData;
    private String eventType;
    private LocalDateTime eventDatetime;
    private String userId;
    private String pcu;
    private String shopOrderBO;
    private String operation_bo;
    private String workcenterId;
    private String resourceId;
    private String routerBO;
    private String itemBO;
    private String dcGrp;
    private String dataField;
    private String dataValue;
    private String component;
    private String nc;
    private String metaData;
    private String qty;
    private String workInstructionBo;
    private String comments;
    private String reasonCode;
    private String shiftId;
    private LocalDateTime shiftCreatedDatetime;
    private LocalDateTime shiftStartTime;
    private LocalDateTime shiftEndTime;
    private Integer shiftAvailableTime;
    private Integer totalBreakHours;
    private Double plannedCycleTime;
    private Double actualCycleTime;
    private Double manufactureTime;
    private Integer quantityStarted;
    private Integer quantityCompleted;
    private Integer quantityScrapped;
    private Integer quantityRework;
    private String status;
    private Boolean isQualityImpact;
    private Boolean isPerformanceImpact;
    private LocalDateTime entryTime;
    private String instructionType;
    private String signoffUser;
    private Integer Active;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String topic;
    private LocalDateTime startDateTime;
    //private String qtyComplete;
    //private String qtyScrap;
    //private String timestamp;
    //private String eventPerformance;
    //private String description;
    //private String shiftType;
    //private LocalDate startDate;
    //private LocalDate endDate;
}
