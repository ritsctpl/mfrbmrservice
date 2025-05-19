package com.rits.performanceservice.dto;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogMongo {
    private Integer eventId;
    private String eventType;
    private String userId;
    private String pcuBO;
    private String operationBO;
    private String shoporderBO;
    private String workcenterBO;
    private String resourceBO;
    private String eventData;
    private String dc_grp;
    private String data_field;
    private String data_value;
    private String component;
    private String itemBO;
    private String nc;
    private String meta_data;
    private Integer qty;
    private String workinstructionBO;
    private String site;
    private String comments;
    private String reasonCode;
    private int active;
    private String shiftName;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private String totalBreakHours;
    private Integer qtyComplete;
    private Integer qtyScrap;
    private String status;
    private String actualCycleTime;
    private String manufactureTime;
    private LocalDateTime timestamp;
}
