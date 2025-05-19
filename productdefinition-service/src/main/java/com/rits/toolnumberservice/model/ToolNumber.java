package com.rits.toolnumberservice.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Document(collection = "R_TOOL_NUMBER")
public class ToolNumber {

    @Id
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
    private int calibrationCount;
    private int maximumCalibrationCount;
    private String expirationDate;
    private String toolGroupSetting;
    private LocalDateTime durationExpiration;
    private List<MeasurementPoints> measurementPointsList;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

    public List<MeasurementPoints> getMeasurementPointsList() {
        return measurementPointsList;
    }

//    public void setMeasurementPointsList(List<MeasurementPoints> measurementPointsList) {
//        this.measurementPointsList = measurementPointsList;
//    }
}
