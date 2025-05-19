package com.rits.quality.service;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.quality.dto.*;
import com.rits.quality.model.ProductionQuality;

import java.util.List;

public interface QualityService {
    OverallQualityResponse getOverallQuality(OeeFilterRequest request) throws Exception;

    QualityByTimeResponse getQualityByTime(OeeFilterRequest request);

    QualityByShiftResponse getQualityByShift(OeeFilterRequest request);
    List<ProductionQualityDTO> getQualityByWorkcenter(OeeFilterRequest request);
//    List<ProductionQualityDTO> getQualityByItem(OeeFilterRequest request);
    List<ProductionQualityDTO> getQualityByDateRange(OeeFilterRequest request);
    List<ProductionQuality> getQualityByDateTime(OeeFilterRequest request);
    List<ProductionQuality> getQualityByCombination(OeeFilterRequest request);
//    List<ProductionQualityDTO> getQualityByResource(OeeFilterRequest request);
    QualityByMachineResponse getQualityByMachine(OeeFilterRequest request);
    QualityByProductResponse getQualityByProduct(OeeFilterRequest request);
    Boolean calculateQuality(OeeFilterRequest qualityRequest);



    //filters
    DefectsByReasonResponse getDefectsByReason(OeeFilterRequest request);
    QualityLossByProductionLineResponse getQualityLossByProductionLine(OeeFilterRequest request);
    DefectByProductResponse getDefectByProduct(OeeFilterRequest request);
    //QualityByOperatorResponse getQualityByOperator(OeeFilterRequest request);
    DefectByTimeResponse getDefectByTime(OeeFilterRequest request);
    GoodVsBadQtyForResourceResponse getGoodVsBadQtyForResource(OeeFilterRequest request);
    /*ScrapAndReworkTrendResponse getScrapAndReworkTrend(OeeFilterRequest request);*/

}
