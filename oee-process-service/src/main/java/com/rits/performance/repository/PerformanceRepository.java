package com.rits.performance.repository;

import com.rits.performance.dto.OeePerformanceDTO;
import com.rits.performance.model.OeePerformanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import com.rits.performance.dto.PerformanceResponseDto;

@Repository
public interface PerformanceRepository extends JpaRepository<OeePerformanceEntity, Long> {

    @Query("SELECT new com.rits.performance.dto.PerformanceResponseDto(p.site, p.workcenterId, p.operation, p.operationVersion, p.resourceId, p.item, p.itemVersion, p.shiftId, p.pcu) FROM OeePerformanceEntity p " +
            "WHERE p.createdDatetime >= :startTime and p.site = :site and p.active = 1 " +
            "GROUP BY p.site, p.workcenterId, p.operation, p.operationVersion, p.resourceId, p.item, p.itemVersion, p.shiftId, p.pcu")
    List<PerformanceResponseDto> findUniqueCombinations(LocalDateTime startTime, String site);

    List<OeePerformanceEntity> findByCreatedDatetimeBetweenAndSiteAndActive(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String site,
            Integer active);

//    @Query(value = "SELECT " +
//            "    o.actual_output, " +
//            "    ROUND(CAST(o.planned_output AS numeric), 2) AS planned_output, " +
//            "    ROUND(CAST(o.performance_percentage AS numeric), 2) AS performance, " +
//            "    o.item, " +
//            "    o.operation, " +
//            "    o.resource_id, " +
//            "    o.workcenter_id, " +
//            "    o.batch_number, " +
//            "    o.shift_id " +
//            "FROM public.r_performance o " +
//            "WHERE o.active = 1 " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND (:shoporderId IS NULL OR o.shop_orderbo IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (o.planned_output > 0 OR o.actual_output > 0) " +
//            "AND o.resource_id IS NOT NULL AND o.resource_id <> '' " +
//            "AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "AND o.item IS NOT NULL AND o.item <> '' " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime",
//            nativeQuery = true)
//    List<OeePerformanceEntity> findPerformanceData(
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

    @Query(value = "SELECT new com.rits.performance.dto.OeePerformanceDTO(" +
            "    o.actualOutput, " +
            "    o.plannedOutput, " +  
            "    o.performancePercentage, " +
            "    o.item, " +
            "    o.operation, " +
            "    o.resourceId, " +
            "    o.workcenterId, " +
            "    o.batchNumber, " +
            "    o.shiftId ) " +
            "FROM OeePerformanceEntity o " +
            "WHERE o.active = 1 " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND (:shoporderId IS NULL OR o.shopOrderBO IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (o.plannedOutput > 0 OR o.actualOutput > 0) " +
            "AND o.resourceId IS NOT NULL AND o.resourceId <> '' " +
            "AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "AND o.item IS NOT NULL AND o.item <> '' " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime")
    List<OeePerformanceDTO> findPerformanceData(
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
