package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.AggregatedQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregatedQualityRepository extends JpaRepository<AggregatedQuality, Long> {
    AggregatedQuality findBySiteAndResourceIdAndItemAndItemVersionAndBatchNumber(
            String site, String resourceId, String item, String itemVersion, String batchNumber);

    AggregatedQuality findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndBatchNumber(
            String site, String shiftId, String resourceId, String item, String itemVersion, String batchNumber);

    AggregatedQuality findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndBatchNumberAndOperationAndOperationVersionAndShopOrderBO(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String batchNumber,
            String operation,
            String operationVersion,
            String shopOrderBO
    );
}


