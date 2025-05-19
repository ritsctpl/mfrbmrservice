package com.rits.toolgroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "R_TOOL_GROUP")
public class ToolGroup {

    @Id
    private String handle;
    private String site;
    private String toolGroup;
    private String description;
    private String status;
    private String trackingControl;
    private String location;
    private int toolQty;
    private String timeBased;
    private String erpGroup;
    private List<Attachment> attachmentList;
    private String calibrationType;
    private String startCalibrationDate;
    private String calibrationPeriod;
    private String calibrationCount;
    private String expirationDate;
    private String maximumCalibrationCount;
    private int duration;
    private int currentCalibrationCount;
    private List<String> tags;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}