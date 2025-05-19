package com.rits.auditlogservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_AUDIT_LOG")
public class AuditLog {
   public String handle;
   public String site;
   public String change_type;
   public String action_code;
   public String action_detail;
   public String action_detail_handle;
   public String activity;
   public String date_time;
   public String crew;
   public String userId;
   public String pcu;
   public String process_lot;
   public String operation;
   public String operation_revision;
   public String item;
   public String item_revision;
   public String router;
   public String router_revision;
   public String stepId;
   public String substep;
   public String substepId;
   public String resrce;
   public String work_center;
   public String qty;
   public String rework;
   public String reporting_center_bo;
   public String shop_order_bo;
   public String partition_date;
   public String lcc_bo;
   public String action_span;
   public String prev_site;
   public String txnId;
   public String created_date_time;
   public String modified_date_time;
   public String category;
}
