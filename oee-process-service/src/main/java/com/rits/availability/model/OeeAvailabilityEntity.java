package com.rits.availability.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

import javax.persistence.*;

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
    private Double actualAvailableTime;
    private Double runtime;
    private Double downtime;
    private Double shiftBreakDuration;
    private Double nonProductionDuration;
    private Double availabilityPercentage;
    private Boolean isPlannedDowntimeIncluded;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private String shiftRef;
    private Integer active;

}
