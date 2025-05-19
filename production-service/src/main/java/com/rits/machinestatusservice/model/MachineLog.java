package com.rits.machinestatusservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "r_machinelog")
public class MachineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int machineLogId;

    private int active;
    private String itemId;
    private String logEvent;
    private String logMessage;
    private String operationId;
    private String resourceId;
    private String workcenterId;
    private String shiftId;
    private String siteId;
    private String shiftStartTime;
    private String createdDate;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime shiftBreakCreatedDateTime;
    private LocalDateTime shiftCreatedDateTime;


}
