package com.rits.oeeservice.dto;

import com.rits.oeeservice.model.Oee;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FinalOEE {
    private List<Oee> oeeData;
    private List<OEEByItem> oeeByItemData;
    private List<OEEByResource> oeeByResourceData;
    private List<AvailabilityByResource> availabilityByResourceData;
    private List<DownTimeByReason> downTimeByReasonData ;
    private List<DownTimeByResource> downTimeByResourceData ;
    private List<DownTimeByResourcePerDay> downTimePerDayData ;
    private List<SpeedLossByResource> speedLossByResourceData ;
    private List<PerformanceByResourcePerDay> performanceByResourcePerDayData ;
    private List<QualityByResourcePerDay> qualityByResourcePerDayData;
    private List<TrendAnalysis> trendAnalysisData;
    private List<ProductionComparison> productionComparisonData;
    private List<ScrapRateByResource> scrapRateData;
    private List<TotalCountByResource> totalCountByResourceData;
    private double availability;
    private double performance;
    private double quality;
    private double oee;
    // Constructors, getters, and setters...


}
