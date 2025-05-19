package com.rits.productionlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "r_availability")
public class OeeAvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String site;
    private String resourceId;
    private String workcenterId;
    private String batchNumber;
    private String shiftId;
    private LocalDate availabilityDate;
    private Double plannedOperatingTime;
    private Double runtime;
    private Double downtime;
    private Double shiftBreakDuration;
    private Double nonProductionDuration;
    private Double availabilityPercentage;
    private Boolean isPlannedDowntimeIncluded;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String shiftRef;
    private Integer active;
    private Double actualAvaialbleTime;

}
