package com.rits.assemblyservice.dto;

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
    private String site;
    private String qty;
    private String active;
    private String timestamp;
}
