package com.rits.auditlogservice.service;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "MACHINE_STATUS_LOG")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MachineStatus {
    @Id
    private int uniqueId;
    private String event;
    private String resource;
    private String workcenter;
    private String shift;
    private String downtimeStart;
    private String downtimeEnd;
    private String meantime;
    private String shiftStartTime;
    private String shiftEndTime;
    private String shiftAvailableTime;
    private String breakHours;
    private String createdDateTime;
    private String createdDate;
    private String site;
    private String reasonCode;
    private int processed;
    private int active;
}
