package com.rits.signoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private int active;
    private String site;
    private String shiftName;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private String totalBreakHours;
    private String status;
    private LocalDateTime timestamp;
}
