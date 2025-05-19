package com.rits.pcustepstatus.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetPcuStepStatusRequest {

   private String site;
   private String operation;
   private String operationVersion;
   private String pcu;
   private String routingBO;
   private String routingDescription;
   private String pcuStepStatus;
   private String shopOrder;
   private String itemBO;
   private String resource;
   private String user;
   private String holdId;
   private boolean shopOrderBO;
   private List<RoutingStep> routingStepList;
}
