package com.rits.oeeservice.service;

import com.rits.oeeservice.dto.*;
import com.rits.oeeservice.model.Oee;
import com.rits.qualityservice.dto.QualityRequestList;

import java.util.List;

public interface OeeService {
    Boolean calculateOee(OeeRequestList oeeRequestList)throws Exception;

    List<Oee> calculateOeeForLive(OeeRequestList oeeRequestList)throws Exception;

    List<AvailabilityByResource>  getAvailabilityByResource(List<Oee> oeeData);

    List<DownTimeByResource> getDownTimeByResource(List<Oee> oeeData);

    List<DownTimeByReason> getDownTimeByReason(List<Oee> oeeData);

    List<Oee> getOEE(FilterObjects filterObjects);


    List<DownTimeByResourcePerDay> getDownTimePerDay(List<Oee> oeeData);

    List<OEEByItem> getOEEByItem(List<Oee> filterObjects);
    List<TrendAnalysis> getTrendAnalysis(List<Oee> oeeData);
    List<ProductionComparison> getComparison(List<Oee> oeeData);

    List<ScrapRateByResource> getScrap(List<Oee> oeeData);

    List<TotalCountByResource> getTotalCountByResource(List<Oee> oeeData);
    FinalOEE getOEECompleteResult(FilterObjects filterObjects);

    FinalOEE getOEECompleteResultForLiveData(FilterObjects filterObjects);

    OeeRequestList getQualityToOeeRequest(QualityRequestList qualityRequestList);
    FinalOEE retrieveOee(FilterObjects filterObjects) throws Exception;
}
