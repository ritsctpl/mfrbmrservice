package com.rits.overallequipmentefficiency.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "R_MACHINELOG")
public class MachineLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Machine_Log_ID")
    private Long machineLogId;

    @Column(name = "Site_ID")
    private String siteId;

    @Column(name = "Shift_ID")
    private String shiftId;

    @Column(name = "shift_created_date_time")
    private LocalDateTime shiftCreatedDateTime;

    @Column(name = "shift_break_created_date_time")
    private LocalDateTime shiftBreakCreatedDateTime;

    @Column(name = "Workcenter_ID")
    private String workcenterId;

    @Column(name = "Resource_ID")
    private String resourceId;

    @Column(name = "Item_ID")
    private String itemId;

    @Column(name = "Operation_ID")
    private String operationId;

    @Column(name = "Log_Message")
    private String logMessage;

    @Column(name = "Log_Event")
    private String logEvent;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "modified_date_time")
    private LocalDateTime modifiedDateTime;

    @Column(name = "Active")
    private Integer active;
}
