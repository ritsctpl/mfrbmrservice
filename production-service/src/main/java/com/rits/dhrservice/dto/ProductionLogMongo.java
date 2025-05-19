package com.rits.dhrservice.dto;

import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductionLogMongo {
    private String eventId;
    private String eventType;
    private String userId;
    private String pcuBO;
    private String operationBO;
    private String shoporderBO;
    private String workcenterBO;
    private String routerBO;
    private String resourceBO;
    private String eventData;
    private String dc_grp;
    private String data_field;
    private String data_value;
    private String component;
    private String itemBO;
    private String nc;
    private String meta_data;
    private String qty;
    private String workinstructionBO;
    private String site;
    private String comments;
    private String reasonCode;
    private String active;
    private String shiftName;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftAvailableTime;
    private String totalBreakHours;
    private String qtyComplete;
    private String qtyScrap;
    private String status;
    private String actualCycleTime;
    private String manufactureTime;
    private String timestamp;
    private String instructionType;
    private String description;
}
