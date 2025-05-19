package com.rits.workinstructionservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogRequest {
    private String eventId;
    private String eventType;
    private String userId;
    private String pcuBO;
    private String shopOrderBO;
    private String operation_bo;
    private String workCenterBO;
    private String resourceBO;
    private String eventData;
    private String itemBO;
    private String dc_grp;
    private String data_field;
    private String data_value;
    private String component;
    private String nc;
    private String meta_data;
    private Integer qty;
    private String workinstruction_BO;
    private String site;
    private String comments;
    private String reasonCode;
    private int active;
    private String topic;
    private String status;
    private LocalDateTime timestamp;
    private String instructionType;
    private String description;

    private String shiftName;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftAvailableTime;
    private String totalBreakHours;
    private String qtyComplete;
    private String qtyScrap;
    private String actualCycleTime;
    private String manufactureTime;
    private String entryTime;
    private String eventPerformance;
    private String routerBO;
}
