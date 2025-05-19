package com.rits.oeeservice.dto;

import com.rits.availability.dto.GraphOeeAvailabilityDTO;
import com.rits.availability.dto.OeeAvailabilityDTO;
import com.rits.availability.model.OeeAvailabilityEntity;
import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AggregatedOee;
import com.rits.performance.dto.OeePerformanceDTO;
import com.rits.quality.dto.ProductionQualityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperatorReportResponse {

    private List<OeeDTO>  r_oee;
    private List<AggregatedOeeDTO> r_aggregated_oee;
    private List<OverAllDataDTO> r_overall;
    private List<OeePerformanceDTO> r_performance;
    private List<ProductionQualityDto> r_quality;
    private List<OeeAvailabilityDTO> r_availability;
    private List<AggregateOeeByMachineDTO> r_machine;
    private List<OeeWorkCenterDto> r_workcenter;
    private List<AggregatedTimePeriodDto> r_time_overall;
    private List<AggregatedTimePeriodGraphDto> r_time_graph;
    private List<DurationAggregatedTimePeriodDto> r_day;
    private List<DurationAggregatedTimePeriodDto> r_month;
    private List<DurationAggregatedTimePeriodDto> r_year;
    private List<GraphOeeAvailabilityDTO> r_availability_graph;
    private List<AggregatedOeeQualityDTO> r_quality_graph;
    private List<PerformanceGraphAggregateOeeDTO> r_performance_graph;
}