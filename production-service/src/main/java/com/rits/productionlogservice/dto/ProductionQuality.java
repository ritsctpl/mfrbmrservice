package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionQuality {

    private Long id;

    private String site;
    private String batch_number;
    private String workcenter_id;
    private String reason;
    private String operation;
    private String operation_version;
    private String resource_id;
    private String item;
    private String item_version;
    private String shift_id;
    private String pcu;
    private String shop_order;
    private Double good_quantity;
    private Double bad_quantity;
    private Double total_quantity;
    private Double quality_percentage;
    private LocalDateTime calculation_timestamp;
    private LocalDateTime created_date_time;
    private LocalDateTime updated_date_time;
    private Integer active;
    private String user_id;
}