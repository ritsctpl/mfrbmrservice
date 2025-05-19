package com.rits.productionlogservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_PRODUCTION_LOG")
//@CompoundIndex(def = "{'itemBO': 1, 'operationBO': 1, 'routerBO': 1, 'resourceBO': 1}")
public class ProductionLogMongo {

    private String eventId;
    private String eventData;
    private String eventType;
    private LocalDateTime eventDatetime;
    private String userId;
    private String pcu;
    private String shoporderBo;
    private String operation;
    private String operationVersion;
    private String workcenterId;
    private String resourceId;
    private String routerBo;
    private String routerVersion;
    private String item;
    private String itemVersion;
    private String dcGrp;
    private String dataField;
    private String dataValue;
    private String component;
    private String nc;
    private String metaData;
    private Integer qty;
    private String workInstructionBo;
    private String comments;
    private String reasonCode;
    private String site;
    private String shiftId;
    private LocalDateTime shiftCreatedDatetime;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Integer shiftAvailableTime;
    private Integer totalBreakHours;
    private Double plannedCycleTime;
    private Double actualCycleTime;
    private Double manufactureTime;
    private Integer quantityStarted;
    private Integer quantityCompleted;
    private Integer quantityScrapped;
    private Integer quantityRework;
    private Boolean isQualityImpact;
    private Boolean isPerformanceImpact;
    private Integer active;
    private String status;
    private String instructionType;
    private String signoffUser;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;

    private String batchNo;
    private String orderNumber;
    private String phaseId;
    private String material;
    private String materialVersion;
}
