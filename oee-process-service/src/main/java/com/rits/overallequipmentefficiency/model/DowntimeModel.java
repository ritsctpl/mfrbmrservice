package com.rits.overallequipmentefficiency.model;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_downtime")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class DowntimeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceId;
    private String workcenterId;
    private String site;
    private String shiftId;
    private LocalDateTime shiftCreatedDateTime;
    private String shiftBreakStartDatetime;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
    //@Column(name = "downtime_duration", insertable = false, updatable = false)
    private Long downtimeDuration;
    private Long plannedOperatingTime;
    private Integer downtEvent;
    private String reason;
    private String rootCause;
    private String commentUsr;
    private String downtimeType;
    private Integer active;

    //@Column(name = "is_oee_impact", insertable = false, updatable = false)
    private Integer isOeeImpact;
    private String shiftRef;

    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}

