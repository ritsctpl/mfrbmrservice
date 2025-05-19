package com.rits.batchnoinqueue.dto;

import com.rits.batchnoinqueue.model.BatchNoInQueue;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoInQueueRequest {

  private String site;
  private LocalDateTime dateTime;
  private String batchNo;
  private String batchNoHeaderBO;
  private String material;
  private String materialVersion;
  private String recipe;
  private String recipeVersion;
  private String batchNoRecipeHeaderBO;
  private String phaseId;
  private String phaseSequence;
  private String operation;
  private String opSequence;
  private BigDecimal quantityBaseUom;
  private BigDecimal quantityMeasuredUom;
  private String baseUom;
  private String measuredUom;
  private LocalDateTime queuedTimestamp;
  private String resource;
  private String workcenter;
  private String user;
  private BigDecimal qtyToComplete;
  private BigDecimal qtyInQueue;
  private String orderNumber;
  private boolean qualityApproval;
  private boolean qualityCheck;
  private boolean operatorCheck;
  private int maxRecord;
  private int active;

  private LocalDateTime createdDateTime;
  private LocalDateTime modifiedDateTime;
  private String createdBy;
  private String modifiedBy;

}
