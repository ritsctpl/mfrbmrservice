package com.rits.dccollect.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParametricPreSave {
//   private String parametricPreSaveBO;
//   private String parameterBo;
//   private String dcGroupBO;
//   private String actualValue;
//   private String dcComment;
//   private String required;
//   private String representation;
//   private String fileAttachmentIds;
//   private String userBo;
//   private String pcuBO;
//   private String operationBO;
//   private String resourceBO;
//   private String workCenterBO;
//   private String routerBO;
//   private String stepID;
//   private String timeProcessed;



      private String parameterBo;
      private String dcGroupBO;
      private String description;
      private String measureStatus;
      private String measureType;
      private String unitOfMeasure;
      private String dataType;
      private String highLimit;
      private String lowLimit;
      private String expected;
      private String actualValue;
      private String actualNum;
      private String dcComment;
      private String usedLimitSeq;
      private String testDateTime;
      private String elapsedTime;
      private String originalActual;
      private String originalDcComment;
      private String originalTestDateTime;
      private String edited;
      private String editedUserBO;
      private String editedDateTime;
      private String internalMeasureID;
      private String pcuBO;
      private String userBO;
      private boolean overrideUserBO;
      private String dcGroupVersion;
      private String erpSent;
      private boolean isQMAccepted;
   }
