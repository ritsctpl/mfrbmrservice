package com.rits.productionlogservice.model;

import lombok.*;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "R_PRODUCTION_LOG")
public class ProductionLog {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Long id;
   private String event_id;
   private String event_data;
   private String event_type;
   private LocalDateTime event_datetime;
   private String user_id;
   private String pcu;
   private String shop_order_bo;
   private String operation;
   private String operation_version;
   private String workcenter_id;
   private String resource_id;
   private String router_bo;
   private String router_version;
   private String item;
   private String item_version;
   private String dc_grp;
   private String data_field;
   private String data_value;
   private String component;
   private String nc;
   private String meta_data;
   private Integer qty;
   private String work_instruction_bo;
   private String comments;
   private String reason_code;
   private String site;
   private String shift_id;
   private LocalDateTime shift_created_datetime;
   private LocalTime shift_start_time;
   private LocalTime shift_end_time;
   private Integer shift_available_time;
   private Integer total_break_hours;
   private Double planned_cycle_time;
   private Double actual_cycle_time;
   private Double manufacture_time;
   private Integer quantity_started;
   private Integer quantity_completed;
   private Integer quantity_scrapped;
   private Integer quantity_rework;
   private Boolean is_quality_impact;
   private Boolean is_performance_impact;
   private Integer active;
   private String status;
   private String instruction_type;
   private String signoff_user;
   private LocalDateTime created_datetime;
   private LocalDateTime updated_datetime;
   private String batchNo;
   private String orderNumber;
   private String phaseId;
   private String material;
   private String materialVersion;
}