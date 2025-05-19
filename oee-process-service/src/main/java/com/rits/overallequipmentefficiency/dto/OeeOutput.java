package com.rits.overallequipmentefficiency.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import com.rits.overallequipmentefficiency.model.OeeModel;
import com.rits.overallequipmentefficiency.model.PerformanceModel;
import com.rits.overallequipmentefficiency.model.QualityModel;
import lombok.*;
import javax.persistence.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OeeOutput {
    private AvailabilityEntity availabilityEntity;
    private CycleTime cycleTime;
    private PerformanceModel performanceEntity;
    private QualityModel qualityModel;
    private OeeModel oeeModel;
}