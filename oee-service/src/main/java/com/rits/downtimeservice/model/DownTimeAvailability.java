package com.rits.downtimeservice.model;

import com.rits.performanceservice.dto.Combinations;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "R_DOWNTIME_AVAILABILITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeAvailability {
    @Id
    private int uniqueId;
    private String site;
    private String resourceId;
    private String createdTime;
    private String createdDateTime;
    private String shift;
    private String entryTime;
    private int plannedProductionTime;
    private int totalDowntime;
    private int operatingTime;
    private int breakHours;
    private int availability;
    private int active;
    private String event;
    private String shiftStartDate;
    private int mcBreakDownHours;
    private String shiftEndDate;
    private Boolean processed;
    private String reasonCode;
    private List<Combinations> combinations;
}
