package com.rits.toollogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolNumber {
    private String handle;
    private String site;
    private String toolNumber;
    private int duration;
    private int currentCalibrationCount;
    private int currentCount;
    private String description;
    private String status;
    private String toolGroup;
    private int qtyAvailable;
    private String erpEquipmentNumber;
    private String erpPlanMaintenanceOrder;
    private int toolQty;
    private String location;
    private String calibrationType;
    private String startCalibrationDate;
    private String calibrationPeriod;
    private String calibrationCount;
    private String maximumCalibrationCount;
    private String expirationDate;
    private String toolGroupSetting;
    private LocalDateTime durationExpiration;

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
