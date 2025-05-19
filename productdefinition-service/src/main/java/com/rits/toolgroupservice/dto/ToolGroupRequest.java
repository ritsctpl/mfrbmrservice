package com.rits.toolgroupservice.dto;

import com.rits.toolgroupservice.model.Attachment;
import com.rits.toolgroupservice.model.CustomData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolGroupRequest {
    private String id;
    private String site;
    private String toolGroup;
    private String handle;
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
    private List<CustomData> customDataList;
    private boolean active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
}
