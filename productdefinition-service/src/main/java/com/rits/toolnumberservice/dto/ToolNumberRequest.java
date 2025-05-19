package com.rits.toolnumberservice.dto;

import com.rits.toolnumberservice.model.CustomData;
import com.rits.toolnumberservice.model.MeasurementPoints;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ToolNumberRequest {
    private String site;
    private String toolNumber;
    private String handle;
    private String description;
    private String status;
    private String toolGroup;
    private int qtyAvailable;
    private String erpEquipmentNumber;
    private String erpPlanMaintenanceOrder;
    private int toolQty;
    private int duration;
    private String location;
    private String calibrationType;
    private String startCalibrationDate;
    private String calibrationPeriod;
    private int calibrationCount;
    private int maximumCalibrationCount;
    private String expirationDate;
    private String toolGroupSetting;
    private List<MeasurementPoints> measurementPointsList;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
}
