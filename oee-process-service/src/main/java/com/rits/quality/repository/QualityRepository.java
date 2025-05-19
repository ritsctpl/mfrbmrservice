package com.rits.quality.repository;

import com.rits.quality.dto.ProductionQualityDTO;
import com.rits.quality.dto.ProductionQualityDto;
import com.rits.quality.model.ProductionQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualityRepository extends JpaRepository<ProductionQuality, Long> {
    @Query("SELECT new com.rits.quality.dto.ProductionQualityDTO(avg(q.goodQuantity), avg(q.badQuantity), avg(q.totalQuantity), avg(q.qualityPercentage)) " +
            "FROM ProductionQuality q " +
            "WHERE q.workcenterId IN (:workcenters) " +
            "AND q.active = 1 AND q.site = :site AND q.createdDateTime BETWEEN :startTime AND :endTime " +
            "GROUP BY q.workcenterId")
    List<ProductionQualityDTO> findQualityByWorkcenters(@Param("workcenters") List<String> workcenters, @Param("site") String site, @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

//    @Query("SELECT new com.rits.quality.dto.ProductionQualityDTO(round(avg(q.goodQuantity),2) as goodQuantity, round(avg(q.badQuantity),2) as badQuantity, " +
//            "round(avg(q.totalQuantity),2) as totalQuantity, round(avg(q.qualityPercentage),2) as qualityPercentage) " +
//            "FROM ProductionQuality q WHERE q.item IN :item AND q.active = 1 AND q.site = :site group by q.item")
//    List<ProductionQualityDTO> findQualityByItem(@Param("item") List<String> item);
//
//    @Query("SELECT new com.rits.quality.dto.ProductionQualityDTO(round(avg(q.goodQuantity),2) as goodQuantity, round(avg(q.badQuantity),2) as badQuantity, " +
//            "round(avg(q.totalQuantity),2) as totalQuantity, round(avg(q.qualityPercentage),2) as qualityPercentage) " +
//            "FROM ProductionQuality q WHERE q.resourceId IN :resource AND q.active = 1 AND q.site = :site group by q.resourceId")
//    List<ProductionQualityDTO> findQualityByResource(@Param("resource") List<String> resource, @Param("site") String site);

    @Query("SELECT new com.rits.quality.dto.ProductionQualityDTO(avg(q.goodQuantity), avg(q.badQuantity), avg(q.totalQuantity), avg(q.qualityPercentage)) " +
            "FROM ProductionQuality q " +
            "WHERE q.createdDateTime between :startTime AND :endTime AND q.active = 1 AND q.site = :site " +
            "GROUP BY DATE(q.createdDateTime)")
    List<ProductionQualityDTO> findQualityByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("site") String site);
    @Query("SELECT q.site,q.workcenterId, q.shiftId, q.item, q.itemVersion, " +
            "q.operation, q.operationVersion, q.batchNumber, q.shopOrder, " +
            "AVG(q.qualityPercentage), q.resourceId " +
            "FROM ProductionQuality q " +
            "WHERE q.site = :site " +
            "AND q.createdDateTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY q.site, q.workcenterId, q.shiftId, q.item, q.itemVersion, " +
            "q.operation, q.operationVersion, q.batchNumber, q.shopOrder ,q.resourceId")
    List<Object[]> findQualityPercentageAverageBySiteAndDateRange(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT q FROM ProductionQuality q " +
            "WHERE q.site = :site " +
            "AND q.createdDateTime BETWEEN :startDateTime AND :endDateTime")
    List<ProductionQuality> findQualityBySiteAndDateRange(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.good_quantity AS NUMERIC), 2) AS good_quantity, " +
//            "    ROUND(CAST(o.bad_quantity AS NUMERIC), 2) AS bad_quantity, " +
//            "    ROUND(CAST(o.plan AS NUMERIC), 2) AS plan, " +
//            "    ROUND(CAST(o.quality_percentage AS NUMERIC), 2) AS quality_percentage, " +
//            "    o.item, " +
//            "    o.operation, " +
//            "    o.shift_id, " +
//            "    o.resource_id, " +
//            "    o.workcenter_id, " +
//            "    o.batch_number " +
//            "FROM public.r_quality o " +
//            "WHERE o.active = 1 " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND (:shoporderId IS NULL OR o.shop_order IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (o.plan > 0 OR o.total_quantity > 0) " +
//            "AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "AND o.resource_id IS NOT NULL AND o.resource_id <> '' " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime",
//            nativeQuery = true)
//    List<ProductionQuality> findQualityData(
//            @Param("site") String site,
//            @Param("batchNumber") List<String> batchNumber,
//            @Param("operation") List<String> operation,
//            @Param("item") List<String> item,
//            @Param("resource") List<String> resource,
//            @Param("workCenter") List<String> workCenter,
//            @Param("shoporderId") List<String> shoporderId,
//            @Param("shiftId") List<String> shiftId,
//            @Param("startTime") LocalDateTime startTime,
//            @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT new com.rits.quality.dto.ProductionQualityDto( " +
            "    o.goodQuantity, " +
            "    o.badQuantity, " +
            "    o.plan, " +
            "    o.qualityPercentage, " +
            "    o.item, " +
            "    o.operation, " +
            "    o.shiftId, " +
            "    o.resourceId, " +
            "    o.workcenterId, " +
            "    o.batchNumber) " +
            "FROM ProductionQuality o " +
            "WHERE o.active = 1 " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND (:shoporderId IS NULL OR o.shopOrder IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (o.plan > 0 OR o.totalQuantity > 0) " +
            "AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "AND o.resourceId IS NOT NULL AND o.resourceId <> '' " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime")
    List<ProductionQualityDto> findQualityData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
