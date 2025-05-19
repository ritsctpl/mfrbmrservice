package com.rits.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OeeAvailabilityDTO {
    private String resourceId;
    private double plannedOperatingTime;
    private double downtime;
    private double availabilityPercentage;
    private String workcenterId;
    private String shiftId;
}
