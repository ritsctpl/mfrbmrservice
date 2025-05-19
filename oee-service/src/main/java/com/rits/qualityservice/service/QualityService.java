package com.rits.qualityservice.service;

import com.rits.oeeservice.model.Oee;
import com.rits.performanceservice.dto.PerformanceRequestList;
import com.rits.qualityservice.dto.ProductionLogQualityResponse;
import com.rits.qualityservice.dto.ProductionLogRequest;
import com.rits.qualityservice.dto.QualityRequestList;

import java.util.List;

public interface QualityService {
    List<ProductionLogQualityResponse> getTotalQuantities(ProductionLogRequest productionLogRequest)throws Exception;

    QualityRequestList calculateQuality(QualityRequestList qualityRequestList)throws Exception;

    QualityRequestList getPerformanceToQualityRequest(PerformanceRequestList performanceRequestList);
    List<Oee> calculateQualityForLiveData(QualityRequestList qualityRequestList)throws Exception;
}
