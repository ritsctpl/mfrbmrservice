package com.rits.performance.dto;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PartsToBeProduced {
    private String site;
    private String shiftId;
    private String operation;
    private String operationVersion;
    private String resourceId;
    private String item;
    private String itemVersion;
    private String workcenterId;
    private String pcu;
    private Double plannedCycleTime;
    private Double manufacturedTime;
    private Double partsToBeProduced;
    private String batchNo;

}

