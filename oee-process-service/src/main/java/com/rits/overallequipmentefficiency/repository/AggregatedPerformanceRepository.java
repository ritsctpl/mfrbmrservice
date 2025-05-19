package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.AggregatedPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregatedPerformanceRepository extends JpaRepository<AggregatedPerformance, Long> {
    AggregatedPerformance findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndBatchNumber(
            String site, String shiftId, String resourceId, String item, String itemVersion, String batchNumber);

    AggregatedPerformance findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndBatchNumberAndOperationAndOperationVersionAndPcuAndShopOrderBO(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String batchNumber,
            String operation,
            String operationVersion,
            String pcu,
            String shopOrderBO
    );


}

