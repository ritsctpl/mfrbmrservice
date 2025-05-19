package com.rits.downtimeservice.repository;

import com.rits.downtimeservice.model.AggregatedResult;

import java.util.List;

public interface DownTimeRepositoryCustom {
    List<AggregatedResult> aggregateByDateRange(String shiftStartDate, String shiftEndDate);

     List<AggregatedResult> customAggregationForToday();
}
