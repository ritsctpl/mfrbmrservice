package com.rits.overallequipmentefficiency.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionLogDto {
    private String site;
    private String workcenter_id;
    private String operation;
    private String operation_version;
    private String resource_id;
    private String item;
    private String item_version;
    private String shift_id;
    private String pcu;
    private String eventType;
}