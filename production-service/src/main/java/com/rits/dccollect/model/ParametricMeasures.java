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
public class ParametricMeasures {
    private String parameterBo;
    private String dcGroupBO;
    private String description;
    private String dcGroupDescription;
    private String subStepID;
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
    private String operationBO;
    private String itemBO;
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
    private String shopOrder;
    private String routingBO;
    private String resource;
    private String stepID;
    private String workCenter;
    private String pcuBO;
    private String userBO;
    private boolean overrideUserBO;
    private String dcGroupVersion;
    private String erpSent;
    private boolean isQMAccepted;
}
