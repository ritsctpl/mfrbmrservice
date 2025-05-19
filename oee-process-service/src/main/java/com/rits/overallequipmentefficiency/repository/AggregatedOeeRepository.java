package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AggregatedOee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AggregatedOeeRepository extends JpaRepository<AggregatedOee, Long> {
    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersion(
            String site, String shiftId, String resourceId, String item, String itemVersion);

    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndOperationAndOperationVersionAndShopOrderIdAndWorkcenterId(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String operation,
            String operationVersion,
            String shopOrderId,
            String workcenterId
    );

    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndOperationAndOperationVersionAndShopOrderIdAndBatchNumberAndWorkcenterId(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String operation,
            String operationVersion,
            String shopOrderId,
            String batchNumber,
            String workcenterId
    );
    List<AggregatedOee> findByCreatedDatetimeBetween(LocalDateTime start, LocalDateTime end);

    List<AggregatedOee> findByShiftId(String shiftId);

    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE ((:site IS NULL AND a.site IS NULL) OR (:site IS NOT NULL AND a.site = :site)) " +
            "  AND ((:shiftId IS NULL AND a.shiftId IS NULL) OR (:shiftId IS NOT NULL AND a.shiftId = :shiftId)) " +
            "  AND ((:workcenterId IS NULL AND a.workcenterId IS NULL) OR (:workcenterId IS NOT NULL AND a.workcenterId = :workcenterId)) " +
            "  AND ((:resourceId IS NULL AND a.resourceId IS NULL) OR (:resourceId IS NOT NULL AND a.resourceId = :resourceId)) " +
            "  AND ((:item IS NULL AND a.item IS NULL) OR (:item IS NOT NULL AND a.item = :item)) " +
            "  AND ((:itemVersion IS NULL AND a.itemVersion IS NULL) OR (:itemVersion IS NOT NULL AND a.itemVersion = :itemVersion)) " +
            "  AND ((:operation IS NULL AND a.operation IS NULL) OR (:operation IS NOT NULL AND a.operation = :operation)) " +
            "  AND ((:operationVersion IS NULL AND a.operationVersion IS NULL) OR (:operationVersion IS NOT NULL AND a.operationVersion = :operationVersion)) " +
            "  AND ((:shopOrderId IS NULL AND a.shopOrderId IS NULL) OR (:shopOrderId IS NOT NULL AND a.shopOrderId = :shopOrderId)) " +
            "  AND ((:batchNumber IS NULL AND a.batchNumber IS NULL) OR (:batchNumber IS NOT NULL AND a.batchNumber = :batchNumber))")
    AggregatedOee findByUniqueKeys(@Param("site") String site,
                                   @Param("shiftId") String shiftId,
                                   @Param("workcenterId") String workcenterId,
                                   @Param("resourceId") String resourceId,
                                   @Param("item") String item,
                                   @Param("itemVersion") String itemVersion,
                                   @Param("operation") String operation,
                                   @Param("operationVersion") String operationVersion,
                                   @Param("shopOrderId") String shopOrderId,
                                   @Param("batchNumber") String batchNumber);




    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.logDate = :logDate " +
            "  AND a.shiftId IS NOT NULL " +
            "  AND (a.resourceId IS NULL OR a.resourceId = '') " +
            "  AND (a.item IS NULL OR a.item = '') " +
            "  AND (a.itemVersion IS NULL OR a.itemVersion = '') " +
            "  AND (a.operation IS NULL OR a.operation = '') " +
            "  AND (a.operationVersion IS NULL OR a.operationVersion = '') " +
            "  AND (a.shopOrderId IS NULL OR a.shopOrderId = '') " +
            "  AND (a.batchNumber IS NULL OR a.batchNumber = '')")
    List<AggregatedOee> findForDayAggregation(@Param("site") String site,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("logDate") LocalDate logDate);


    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.logDate = :logDate " +
            "  AND a.active = true " +
            "  AND (a.workcenterId IS NOT NULL ) " +
            "  AND (a.resourceId IS NULL OR a.resourceId = '') " +
            "  AND (a.item IS NULL OR a.item = '') " +
            "  AND (a.itemVersion IS NULL OR a.itemVersion = '') " +
            "  AND (a.operation IS NULL OR a.operation = '') " +
            "  AND (a.operationVersion IS NULL OR a.operationVersion = '') " +
            "  AND (a.shopOrderId IS NULL OR a.shopOrderId = '') " +
            "  AND (a.batchNumber IS NULL OR a.batchNumber = '')")
    List<AggregatedOee> findForShiftAggregationByShift(@Param("site") String site,
                                                       @Param("shiftId") String shiftId,
                                                       @Param("logDate") LocalDate logDate);



    @Modifying
    @Query("UPDATE AggregatedOee a SET a.active = false " +
            "WHERE ((:site IS NULL AND a.site IS NULL) OR (:site IS NOT NULL AND a.site = :site)) " +
            "  AND ((:shiftId IS NULL AND a.shiftId IS NULL) OR (:shiftId IS NOT NULL AND a.shiftId = :shiftId)) " +
            "  AND ((:workcenterId IS NULL AND a.workcenterId IS NULL) OR (:workcenterId IS NOT NULL AND a.workcenterId = :workcenterId)) " +
            "  AND ((:resourceId IS NULL AND a.resourceId IS NULL) OR (:resourceId IS NOT NULL AND a.resourceId = :resourceId)) " +
            "  AND ((:item IS NULL AND a.item IS NULL) OR (:item IS NOT NULL AND a.item = :item)) " +
            "  AND ((:itemVersion IS NULL AND a.itemVersion IS NULL) OR (:itemVersion IS NOT NULL AND a.itemVersion = :itemVersion)) " +
            "  AND ((:operation IS NULL AND a.operation IS NULL) OR (:operation IS NOT NULL AND a.operation = :operation)) " +
            "  AND ((:operationVersion IS NULL AND a.operationVersion IS NULL) OR (:operationVersion IS NOT NULL AND a.operationVersion = :operationVersion)) " +
            "  AND ((:shopOrderId IS NULL AND a.shopOrderId IS NULL) OR (:shopOrderId IS NOT NULL AND a.shopOrderId = :shopOrderId)) " +
            "  AND ((:batchNumber IS NULL AND a.batchNumber IS NULL) OR (:batchNumber IS NOT NULL AND a.batchNumber = :batchNumber))")
    void deactivateByUniqueKeys(
            @Param("site") String site,
            @Param("shiftId") String shiftId,
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("shopOrderId") String shopOrderId,
            @Param("batchNumber") String batchNumber);

    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.logDate = :logDate " +
            "  AND a.active = true " +
            "  AND (a.workcenterId = :workcenterId ) " +
            "  AND (a.resourceId IS NULL OR a.resourceId = '') " +
            "  AND (a.item IS NULL OR a.item = '') " +
            "  AND (a.itemVersion IS NULL OR a.itemVersion = '') " +
            "  AND (a.operation IS NULL OR a.operation = '') " +
            "  AND (a.operationVersion IS NULL OR a.operationVersion = '') " +
            "  AND (a.shopOrderId IS NULL OR a.shopOrderId = '') " +
            "  AND (a.batchNumber IS NULL OR a.batchNumber = '')")
    List<AggregatedOee> findForShiftAggregationByWorkcenterIdAndShift(@Param("site") String site,
                                                                    @Param("workcenterId") String workcenterId,
                                                                    @Param("shiftId") String shiftId,
                                                                    @Param("logDate") LocalDate logDate);

//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.availability AS numeric), 2) AS availability, " +
//            "    ROUND(CAST(o.oee AS numeric), 2) AS oee, " +
//            "    ROUND(CAST(o.performance AS numeric), 2) AS performance, " +
//            "    ROUND(CAST(o.quality AS numeric), 2) AS quality, " +
//            "    ROUND(CAST(o.actual_cycle_time AS numeric), 2) AS actual_cycle_time, " +
//            "    o.actual_production_time, " +
//            "    o.actual_time, " +
//            "    o.production_time, " +
//            "    o.total_good_quantity, " +
//            "    o.total_bad_quantity, " +
//            "    o.plan, " +
//            "    o.batch_number, " +
//            "    o.resource_id, " +
//            "    o.workcenter_id, " +
//            "    o.shift_id, " +
//            "    o.operation, " +
//            "    o.item, " +
//            "    o.shop_order_id, " +
//            "    o.category " +
//            "FROM public.r_aggregated_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = (CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END) " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number = ANY(CAST(:batchNumber AS text[]))) " +
//            "AND (:shoporderId IS NULL OR o.shop_order_id = ANY(CAST(:shoporderId AS text[]))) " +
//            "AND (:shiftId IS NULL OR o.shift_id = ANY(CAST(:shiftId AS text[]))) " +
//            "AND (:item IS NULL OR o.item = ANY(CAST(:item AS text[]))) " +
//            "AND (:operation IS NULL OR o.operation = ANY(CAST(:operation AS text[]))) " +
//            "AND (:resource IS NULL OR o.resource_id = ANY(CAST(:resource AS text[]))) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id = ANY(CAST(:workCenter AS text[]))) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND ( " +
//            "    ((:shiftId IS NOT NULL AND (:item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL)) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '') AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:item IS NOT NULL AND (:operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL)) " +
//            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batch_number IS NULL OR o.batch_number = '') " +
//            "     AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:operation IS NOT NULL AND (:item IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL)) " +
//            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((:shoporderId IS NOT NULL AND (:item IS NULL AND :operation IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL)) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '')) " +
//            "    OR " +
//            "    ((:site IS NOT NULL OR :workCenter IS NOT NULL OR :batchNumber IS NOT NULL) " +
//            "     AND (:item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :shiftId IS NULL) " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.item IS NOT NULL AND o.item <> '' " +
//            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END + " +
//            "      CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END + " +
//            "      CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END + " +
//            "      CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END + " +
//            "      CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END + " +
//            "      CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END) > 1 " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '') " +
//            ")", nativeQuery = true)
//    List<AggregatedOee> findAggregatedOeeData(
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

            @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeDTO( " +
            "o.availability, " +
            "o.oee, " +
            "o.performance, " +
            "o.quality, " +
            "o.actualCycleTime, " +
            "o.actualProductionTime, " +
            "o.actualTime, " +
            "o.productionTime, " +
            "o.totalGoodQuantity, " +
            "o.totalBadQuantity, " +
            "o.plan, " +
            "o.batchNumber, " +
            "o.resourceId, " +
            "o.workcenterId, " +
            "o.shiftId, " +
            "o.operation, " +
            "o.item, " +
            "o.shopOrderId, " +
            "o.category ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime ")
    List<AggregatedOeeDTO> findAggregatedOeeData(
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

//    @Query(value = "SELECT " +
//            "    ROUND(AVG(CAST(o.availability AS numeric)), 2) AS availability, " +
//            "    ROUND(AVG(CAST(o.oee AS numeric)), 2) AS oee, " +
//            "    ROUND(AVG(CAST(o.performance AS numeric)), 2) AS performance, " +
//            "    ROUND(AVG(CAST(o.quality AS numeric)), 2) AS quality " +
//            "FROM public.r_aggregated_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:shoporderId IS NULL OR o.shop_order_id IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND ( " +
//            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '') AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batch_number IS NULL OR o.batch_number = '') " +
//            "     AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '')) " +
//            "    OR " +
//            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
//            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.item IS NOT NULL AND o.item <> '' " +
//            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    (CASE WHEN ((:item IS NOT NULL)::integer + (:operation IS NOT NULL)::integer + " +
//            "               (:shoporderId IS NOT NULL)::integer + (:batchNumber IS NOT NULL)::integer + " +
//            "               (:shiftId IS NOT NULL)::integer + (:workCenter IS NOT NULL)::integer) > 1 " +
//            "     THEN true ELSE false END " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '') " +
//            ")", nativeQuery = true)
//    List<AggregatedOee> findOverallData(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.OverAllDataDTO(" +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN (:batchNumber)) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN (:shoporderId)) " +
            "AND (:shiftId IS NULL OR o.shiftId IN (:shiftId)) " +
            "AND (:item IS NULL OR o.item IN (:item)) " +
            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
            "AND (:resource IS NULL OR o.resourceId IN (:resource)) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN (:workCenter)) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "     AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '')) " +
            "    OR " +
            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
            "     AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.item IS NOT NULL AND o.item <> '' " +
            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            ")")
    List<OverAllDataDTO> findOverallData(
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


//    @Query(value = "SELECT " +
//            "    CASE WHEN :resource IS NOT NULL THEN o.resource_id ELSE o.workcenter_id END as value, " +
//            "    ROUND(CAST(o.availability AS numeric), 2) as availability, " +
//            "    ROUND(CAST(o.oee AS numeric), 2) as oee, " +
//            "    ROUND(CAST(o.performance AS numeric), 2) as performance, " +
//            "    ROUND(CAST(o.quality AS numeric), 2) as quality " +
//            "FROM public.r_aggregated_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:shoporderId IS NULL OR o.shop_order_id IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND ( " +
//            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '') AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batch_number IS NULL OR o.batch_number = '') " +
//            "     AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '')) " +
//            "    OR " +
//            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
//            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.item IS NOT NULL AND o.item <> '' " +
//            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    (CASE WHEN ((:item IS NOT NULL)::integer + (:operation IS NOT NULL)::integer + " +
//            "               (:shoporderId IS NOT NULL)::integer + (:batchNumber IS NOT NULL)::integer + " +
//            "               (:shiftId IS NOT NULL)::integer + (:workCenter IS NOT NULL)::integer) > 1 " +
//            "     THEN true ELSE false END " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '') " +
//            ")", nativeQuery = true)
//    List<AggregatedOee> findMachineData(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregateOeeByMachineDTO( " +
            "    o.resourceId, " +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality " +
            ") " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "     AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '')) " +
            "    OR " +
            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
            "     AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.item IS NOT NULL AND o.item <> '' " +
            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    (((CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END)) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ")")
    List<AggregateOeeByMachineDTO> findMachineDataByResource(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregateOeeByMachineDTO( " +
            "    o.workcenterId, " +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality " +
            ") " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "     AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '')) " +
            "    OR " +
            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
            "     AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.item IS NOT NULL AND o.item <> '' " +
            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    (((CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END)) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ")")
    List<AggregateOeeByMachineDTO> findMachineDataByWorkcenter(
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


//    @Query(value = "SELECT " +
//            "    CASE WHEN :resource IS NOT NULL THEN o.resource_id ELSE o.workcenter_id END as value, " +
//            "    ROUND(SUM(CAST(o.total_good_quantity AS numeric)), 2) AS goodquantity, " +
//            "    ROUND(SUM(CAST(o.total_bad_quantity AS numeric)), 2) AS badquantity " +
//            "FROM public.r_aggregated_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:shoporderId IS NULL OR o.shop_order_id IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND ( " +
//            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '') AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batch_number IS NULL OR o.batch_number = '') " +
//            "     AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '')) " +
//            "    OR " +
//            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
//            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.item IS NOT NULL AND o.item <> '' " +
//            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    (CASE WHEN ((:item IS NOT NULL)::integer + (:operation IS NOT NULL)::integer + " +
//            "               (:shoporderId IS NOT NULL)::integer + (:batchNumber IS NOT NULL)::integer + " +
//            "               (:shiftId IS NOT NULL)::integer + (:workCenter IS NOT NULL)::integer) > 1 " +
//            "     THEN true ELSE false END " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '') " +
//            ") " +
//            "GROUP BY o.resource_id, o.workcenter_id",
//            nativeQuery = true)
//    List<Object[]> findQualityGraphData(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeQualityDTO( " +
            "    o.resourceId, " +
            "    SUM(o.totalGoodQuantity), " +
            "    SUM(o.totalBadQuantity)) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ( (:resource IS NOT NULL AND o.category = 'RESOURCE') " +
            "    OR (:resource IS NULL AND o.category = 'WORKCENTER') ) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ( (:shiftId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL) " +
            "      AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "      AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '') ) " +
            "    OR " +
            "    ( (:item IS NOT NULL AND :operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "      AND (o.shopOrderId IS NULL OR o.shopOrderId = '') ) " +
            "    OR " +
            "    ( (:operation IS NOT NULL AND :item IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "      AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '' ) " +
            "    OR " +
            "    ( (:shoporderId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "      AND (o.batchNumber IS NULL OR o.batchNumber = '') ) " +
            "    OR " +
            "    ( (:site IS NOT NULL OR :workCenter IS NOT NULL OR :batchNumber IS NOT NULL) " +
            "      AND :item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :shiftId IS NULL " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "      AND o.item IS NOT NULL AND o.item <> '' " +
            "      AND o.operation IS NOT NULL AND o.operation <> '' " +
            "      AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '' ) " +
            "    OR " +
            "    ( (CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ") " +
            "GROUP BY o.resourceId, o.workcenterId")
    List<AggregatedOeeQualityDTO> findResourceQualityGraphData(
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


    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeQualityDTO( " +
            "    o.workcenterId, " +
            "    SUM(o.totalGoodQuantity), " +
            "    SUM(o.totalBadQuantity)) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ( (:resource IS NOT NULL AND o.category = 'RESOURCE') " +
            "    OR (:resource IS NULL AND o.category = 'WORKCENTER') ) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ( (:shiftId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL) " +
            "      AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "      AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '') ) " +
            "    OR " +
            "    ( (:item IS NOT NULL AND :operation IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "      AND (o.shopOrderId IS NULL OR o.shopOrderId = '') ) " +
            "    OR " +
            "    ( (:operation IS NOT NULL AND :item IS NULL AND :shoporderId IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "      AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '' ) " +
            "    OR " +
            "    ( (:shoporderId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL AND :workCenter IS NULL AND :shiftId IS NULL) " +
            "      AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "      AND (o.batchNumber IS NULL OR o.batchNumber = '') ) " +
            "    OR " +
            "    ( (:site IS NOT NULL OR :workCenter IS NOT NULL OR :batchNumber IS NOT NULL) " +
            "      AND :item IS NULL AND :operation IS NULL AND :shoporderId IS NULL AND :shiftId IS NULL " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "      AND o.item IS NOT NULL AND o.item <> '' " +
            "      AND o.operation IS NOT NULL AND o.operation <> '' " +
            "      AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '' ) " +
            "    OR " +
            "    ( (CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ") " +
            "GROUP BY o.resourceId, o.workcenterId")
    List<AggregatedOeeQualityDTO> findWorkcenterQualityGraphData(
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

//    @Query(value = "SELECT " +
//            "    CASE WHEN :resource IS NOT NULL THEN o.resource_id ELSE o.workcenter_id END as value, " +
//            "    ROUND(CAST(o.performance AS numeric), 2) AS performance " +
//            "FROM public.r_aggregated_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:shoporderId IS NULL OR o.shop_order_id IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND ( " +
//            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '') AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batch_number IS NULL OR o.batch_number = '') " +
//            "     AND (o.shop_order_id IS NULL OR o.shop_order_id = '')) " +
//            "    OR " +
//            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
//            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
//            "     AND (o.batch_number IS NULL OR o.batch_number = '')) " +
//            "    OR " +
//            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
//            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '' " +
//            "     AND o.item IS NOT NULL AND o.item <> '' " +
//            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
//            "     AND o.shop_order_id IS NOT NULL AND o.shop_order_id <> '') " +
//            "    OR " +
//            "    (CASE WHEN ((:item IS NOT NULL)::integer + (:operation IS NOT NULL)::integer + " +
//            "               (:shoporderId IS NOT NULL)::integer + (:batchNumber IS NOT NULL)::integer + " +
//            "               (:shiftId IS NOT NULL)::integer + (:workCenter IS NOT NULL)::integer) > 1 " +
//            "     THEN true ELSE false END " +
//            "     AND o.batch_number IS NOT NULL AND o.batch_number <> '') " +
//            ")", nativeQuery = true)
//    List<AggregatedOee> findPerformanceGraphData(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.PerformanceGraphAggregateOeeDTO( " +
            "    o.resourceId, " +
            "    o.performance ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "     AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '')) " +
            "    OR " +
            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
            "     AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.item IS NOT NULL AND o.item <> '' " +
            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ( (CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ")")
    List<PerformanceGraphAggregateOeeDTO> findResourcePerformanceGraphData(
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

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.PerformanceGraphAggregateOeeDTO( " +
            "    o.workcenterId, " +
            "    o.performance ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND ( " +
            "    ((:shiftId IS NOT NULL AND COALESCE(:item, :operation, :shoporderId, :batchNumber, :workCenter) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '') AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:item IS NOT NULL AND COALESCE(:operation, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.operation IS NULL OR o.operation = '') AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            "     AND (o.shopOrderId IS NULL OR o.shopOrderId = '')) " +
            "    OR " +
            "    ((:operation IS NOT NULL AND COALESCE(:item, :shoporderId, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND o.item IS NOT NULL AND o.item <> '' AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ((:shoporderId IS NOT NULL AND COALESCE(:item, :operation, :batchNumber, :workCenter, :shiftId) IS NULL) " +
            "     AND (o.item IS NULL OR o.item = '') AND (o.operation IS NULL OR o.operation = '') " +
            "     AND (o.batchNumber IS NULL OR o.batchNumber = '')) " +
            "    OR " +
            "    (COALESCE(:site, :workCenter, :batchNumber) IS NOT NULL " +
            "     AND COALESCE(:item, :operation, :shoporderId, :shiftId) IS NULL " +
            "     AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' " +
            "     AND o.item IS NOT NULL AND o.item <> '' " +
            "     AND o.operation IS NOT NULL AND o.operation <> '' " +
            "     AND o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') " +
            "    OR " +
            "    ( (CASE WHEN :item IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :operation IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shoporderId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :batchNumber IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :shiftId IS NOT NULL THEN 1 ELSE 0 END) + " +
            "      (CASE WHEN :workCenter IS NOT NULL THEN 1 ELSE 0 END) > 1 " +
            "      AND o.batchNumber IS NOT NULL AND o.batchNumber <> '') " +
            ")")
    List<PerformanceGraphAggregateOeeDTO> findWorkcenterPerformanceGraphData(
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

    // Derived queries updated to include eventSource.
    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndEventSource(
            String site, String shiftId, String resourceId, String item, String itemVersion, String eventSource);

    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndOperationAndOperationVersionAndShopOrderIdAndWorkcenterIdAndEventSource(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String operation,
            String operationVersion,
            String shopOrderId,
            String workcenterId,
            String eventSource
    );

    AggregatedOee findBySiteAndShiftIdAndResourceIdAndItemAndItemVersionAndOperationAndOperationVersionAndShopOrderIdAndBatchNumberAndWorkcenterIdAndEventSource(
            String site,
            String shiftId,
            String resourceId,
            String item,
            String itemVersion,
            String operation,
            String operationVersion,
            String shopOrderId,
            String batchNumber,
            String workcenterId,
            String eventSource
    );

    List<AggregatedOee> findByShiftIdAndEventSource(String shiftId, String eventSource);

    // Unique keys query updated to include eventSource.
    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE ((:site IS NULL AND a.site IS NULL) OR (:site IS NOT NULL AND a.site = :site)) " +
            "  AND ((:shiftId IS NULL AND a.shiftId IS NULL) OR (:shiftId IS NOT NULL AND a.shiftId = :shiftId)) " +
            "  AND ((:workcenterId IS NULL AND a.workcenterId IS NULL) OR (:workcenterId IS NOT NULL AND a.workcenterId = :workcenterId)) " +
            "  AND ((:resourceId IS NULL AND a.resourceId IS NULL) OR (:resourceId IS NOT NULL AND a.resourceId = :resourceId)) " +
            "  AND ((:item IS NULL AND a.item IS NULL) OR (:item IS NOT NULL AND a.item = :item)) " +
            "  AND ((:itemVersion IS NULL AND a.itemVersion IS NULL) OR (:itemVersion IS NOT NULL AND a.itemVersion = :itemVersion)) " +
            "  AND ((:operation IS NULL AND a.operation IS NULL) OR (:operation IS NOT NULL AND a.operation = :operation)) " +
            "  AND ((:operationVersion IS NULL AND a.operationVersion IS NULL) OR (:operationVersion IS NOT NULL AND a.operationVersion = :operationVersion)) " +
            "  AND ((:shopOrderId IS NULL AND a.shopOrderId IS NULL) OR (:shopOrderId IS NOT NULL AND a.shopOrderId = :shopOrderId)) " +
            "  AND ((:batchNumber IS NULL AND a.batchNumber IS NULL) OR (:batchNumber IS NOT NULL AND a.batchNumber = :batchNumber)) " +
            "  AND a.eventSource = :eventSource")
    AggregatedOee findByUniqueKeys(@Param("site") String site,
                                   @Param("shiftId") String shiftId,
                                   @Param("workcenterId") String workcenterId,
                                   @Param("resourceId") String resourceId,
                                   @Param("item") String item,
                                   @Param("itemVersion") String itemVersion,
                                   @Param("operation") String operation,
                                   @Param("operationVersion") String operationVersion,
                                   @Param("shopOrderId") String shopOrderId,
                                   @Param("batchNumber") String batchNumber,
                                   @Param("eventSource") String eventSource);

    // Aggregation queries updated with eventSource filtering.

    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.logDate = :logDate " +
            "  AND a.shiftId IS NOT NULL " +
            "  AND (a.resourceId IS NULL OR a.resourceId = '') " +
            "  AND (a.item IS NULL OR a.item = '') " +
            "  AND (a.itemVersion IS NULL OR a.itemVersion = '') " +
            "  AND (a.operation IS NULL OR a.operation = '') " +
            "  AND (a.operationVersion IS NULL OR a.operationVersion = '') " +
            "  AND (a.shopOrderId IS NULL OR a.shopOrderId = '') " +
            "  AND (a.batchNumber IS NULL OR a.batchNumber = '') " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedOee> findForDayAggregation(@Param("site") String site,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("logDate") LocalDate logDate,
                                              @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.logDate = :logDate " +
            "  AND a.active = true " +
            "  AND (a.workcenterId IS NOT NULL) " +
            "  AND (a.resourceId IS NULL OR a.resourceId = '') " +
            "  AND (a.item IS NULL OR a.item = '') " +
            "  AND (a.itemVersion IS NULL OR a.itemVersion = '') " +
            "  AND (a.operation IS NULL OR a.operation = '') " +
            "  AND (a.operationVersion IS NULL OR a.operationVersion = '') " +
            "  AND (a.shopOrderId IS NULL OR a.shopOrderId = '') " +
            "  AND (a.batchNumber IS NULL OR a.batchNumber = '') " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedOee> findForShiftAggregationByShift(@Param("site") String site,
                                                       @Param("shiftId") String shiftId,
                                                       @Param("logDate") LocalDate logDate,
                                                       @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedOee a " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.active = true " +
            "  AND a.category = 'WORKCENTER' " +
            "  AND a.logDate = :logDate " +
            "  AND a.eventSource = :eventSource"  +
            "  AND ( a.item IS NULL OR a.item = '' ) " +
            "  AND ( a.itemVersion IS NULL OR a.itemVersion = '' ) " +
            "  AND ( a.operation IS NULL OR a.operation = '' ) " +
            "  AND ( a.operationVersion IS NULL OR a.operationVersion = '' )" +
            "  AND ( a.shopOrderId IS NULL OR a.shopOrderId = '' ) " +
            "  AND ( a.batchNumber IS NULL OR a.batchNumber = '' ) " +
            "  AND ( a.resourceId IS NULL OR  a.resourceId = '' ) "
    )
    List<AggregatedOee> findForShiftAggregationByWorkcenterIdAndShift(@Param("site") String site,
                                                                      @Param("workcenterId") String workcenterId,
                                                                      @Param("shiftId") String shiftId,
                                                                      @Param("logDate") LocalDate logDate,
                                                                      @Param("eventSource") String eventSource);

    // Update operation for deactivation with eventSource filtering.
    @Modifying
    @Query("UPDATE AggregatedOee a SET a.active = false " +
            "WHERE ((:site IS NULL AND a.site IS NULL) OR (:site IS NOT NULL AND a.site = :site)) " +
            "  AND ((:shiftId IS NULL AND a.shiftId IS NULL) OR (:shiftId IS NOT NULL AND a.shiftId = :shiftId)) " +
            "  AND ((:workcenterId IS NULL AND a.workcenterId IS NULL) OR (:workcenterId IS NOT NULL AND a.workcenterId = :workcenterId)) " +
            "  AND ((:resourceId IS NULL AND a.resourceId IS NULL) OR (:resourceId IS NOT NULL AND a.resourceId = :resourceId)) " +
            "  AND ((:item IS NULL AND a.item IS NULL) OR (:item IS NOT NULL AND a.item = :item)) " +
            "  AND ((:itemVersion IS NULL AND a.itemVersion IS NULL) OR (:itemVersion IS NOT NULL AND a.itemVersion = :itemVersion)) " +
            "  AND ((:operation IS NULL AND a.operation IS NULL) OR (:operation IS NOT NULL AND a.operation = :operation)) " +
            "  AND ((:operationVersion IS NULL AND a.operationVersion IS NULL) OR (:operationVersion IS NOT NULL AND a.operationVersion = :operationVersion)) " +
            "  AND ((:shopOrderId IS NULL AND a.shopOrderId IS NULL) OR (:shopOrderId IS NOT NULL AND a.shopOrderId = :shopOrderId)) " +
            "  AND ((:batchNumber IS NULL AND a.batchNumber IS NULL) OR (:batchNumber IS NOT NULL AND a.batchNumber = :batchNumber)) " +
            "  AND a.eventSource = :eventSource")
    void deactivateByUniqueKeys(
            @Param("site") String site,
            @Param("shiftId") String shiftId,
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("shopOrderId") String shopOrderId,
            @Param("batchNumber") String batchNumber,
            @Param("eventSource") String eventSource);

    // Reporting queries – add eventSource filtering.
    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeDTO( " +
            "o.availability, " +
            "o.oee, " +
            "o.performance, " +
            "o.quality, " +
            "o.actualCycleTime, " +
            "o.actualProductionTime, " +
            "o.actualTime, " +
            "o.productionTime, " +
            "o.totalGoodQuantity, " +
            "o.totalBadQuantity, " +
            "o.plan, " +
            "o.batchNumber, " +
            "o.resourceId, " +
            "o.workcenterId, " +
            "o.shiftId, " +
            "o.operation, " +
            "o.item, " +
            "o.shopOrderId, " +
            "o.category ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<AggregatedOeeDTO> findAggregatedOeeData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.OverAllDataDTO(" +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND o.category = CASE WHEN :resource IS NOT NULL THEN 'RESOURCE' ELSE 'WORKCENTER' END " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<OverAllDataDTO> findOverallData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregateOeeByMachineDTO( " +
            "    o.resourceId, " +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality " +
            ") " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<AggregateOeeByMachineDTO> findMachineDataByResource(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregateOeeByMachineDTO( " +
            "    o.workcenterId, " +
            "    o.availability, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality " +
            ") " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<AggregateOeeByMachineDTO> findMachineDataByWorkcenter(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeQualityDTO( " +
            "    o.resourceId, " +
            "    SUM(o.totalGoodQuantity), " +
            "    SUM(o.totalBadQuantity)) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource " +
            "GROUP BY o.resourceId, o.workcenterId")
    List<AggregatedOeeQualityDTO> findResourceQualityGraphData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedOeeQualityDTO( " +
            "    o.workcenterId, " +
            "    SUM(o.totalGoodQuantity), " +
            "    SUM(o.totalBadQuantity)) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource " +
            "GROUP BY o.resourceId, o.workcenterId")
    List<AggregatedOeeQualityDTO> findWorkcenterQualityGraphData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.PerformanceGraphAggregateOeeDTO( " +
            "    o.resourceId, " +
            "    o.performance ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<PerformanceGraphAggregateOeeDTO> findResourcePerformanceGraphData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.PerformanceGraphAggregateOeeDTO( " +
            "    o.workcenterId, " +
            "    o.performance ) " +
            "FROM AggregatedOee o " +
            "WHERE o.active = true " +
            "AND ((:resource IS NOT NULL AND o.category = 'RESOURCE') OR (:resource IS NULL AND o.category = 'WORKCENTER')) " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:shoporderId IS NULL OR o.shopOrderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.eventSource = :eventSource")
    List<PerformanceGraphAggregateOeeDTO> findWorkcenterPerformanceGraphData(
            @Param("site") String site,
            @Param("batchNumber") List<String> batchNumber,
            @Param("operation") List<String> operation,
            @Param("item") List<String> item,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shoporderId") List<String> shoporderId,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("eventSource") String eventSource);

    @Query("SELECT o FROM AggregatedOee o " +
            " WHERE o.site = :site " +
            "  AND o.shiftId = :shiftId " +
            " AND o.workcenterId IN :lineWorkcenters " +
            " AND o.logDate = :logDate " +
            "  AND o.active = true " +
            "  AND o.category = 'WORKCENTER' "+
            "  AND (o.resourceId IS NULL OR o.resourceId = '') " +
            "  AND (o.item IS NULL OR o.item = '') " +
            "  AND (o.itemVersion IS NULL OR o.itemVersion = '') " +
            "  AND (o.operation IS NULL OR o.operation = '') " +
            "  AND (o.operationVersion IS NULL OR o.operationVersion = '') " +
            "  AND (o.shopOrderId IS NULL OR o.shopOrderId = '') " +
            "  AND (o.batchNumber IS NULL OR o.batchNumber = '') " +
            " AND o.eventSource = :eventSource")
    List<AggregatedOee> findForCellShiftAggregation(@Param("site") String site,
                                                    @Param("shiftId") String shiftId,
                                                    @Param("lineWorkcenters") List<String> lineWorkcenters,
                                                    @Param("logDate") LocalDate logDate,
                                                    @Param("eventSource") String eventSource);

    @Query("SELECT o FROM AggregatedOee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.active = TRUE "
            + "AND (:resourceId IS NULL OR o.resourceId IN :resourceId) "
            + "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) "
            + "AND (:workcenterId IS NULL OR o.workcenterId IN :workcenterId) "
            + "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) "
            + "AND (:shopOrderId IS NULL OR o.shopOrderId IN :shopOrderId) "
            + "AND (:item IS NULL OR o.item IN :item) "
            + "AND (:operation IS NULL OR o.operation IN :operation) "
            + "AND o.category = :eventType "
            + "AND o.eventSource = :eventSource "

            + "AND ( "
            + "   (:shiftId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND :shopOrderId IS NULL AND :workcenterId IS NULL "
            + "   AND (o.item IS NULL OR o.item = '') "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   AND (o.shopOrderId IS NULL OR o.shopOrderId = '') "
            + "   ) "
            + "   OR (:item IS NOT NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND :shopOrderId IS NULL AND :workcenterId IS NULL AND :shiftId IS NULL "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   AND (o.shopOrderId IS NULL OR o.shopOrderId = '') "
            + "   ) "
            + "   OR (:operation IS NOT NULL AND :item IS NOT NULL AND :batchNumber IS NOT NULL "
            + "   AND :shopOrderId IS NOT NULL "
            + "   AND (o.item IS NOT NULL AND o.item <> '') "
            + "   AND (o.batchNumber IS NOT NULL AND o.batchNumber <> '') "
            + "   AND (o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') "
            + "   ) "
            + "   OR (:shopOrderId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND (o.item IS NULL OR o.item = '') "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   ) "
            + "   OR ((:site IS NOT NULL OR :workcenterId IS NOT NULL OR :batchNumber IS NOT NULL) "
            + "   AND :item IS NULL AND :operation IS NULL AND :shopOrderId IS NULL AND :shiftId IS NULL "
//            + "   AND (o.batchNumber IS NOT NULL AND o.batchNumber <> '') "
            + "   AND ("
            + "     :checkBatchCondition = false OR ("
            + "       (o.batchNumber IS NOT NULL AND o.batchNumber <> '') AND "
            + "       (o.item IS NOT NULL AND o.item <> '') AND "
            + "       (o.operation IS NOT NULL AND o.operation <> '') AND "
            + "       (o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') "
            + "     )"
            + "   )"
            + "   ) )")
    List<AggregatedOee> findByFilters(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("resourceId") List<String> resourceId,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterId") List<String> workcenterId,
            @Param("batchNumber") List<String> batchNumber,
            @Param("shopOrderId") List<String> shopOrderId,
            @Param("item") List<String> item,
            @Param("operation") List<String> operation,
            @Param("eventType") String eventType,
            @Param("eventSource") String eventSource,
            @Param("checkBatchCondition") Boolean checkBatchCondition
    );


    @Query("SELECT o FROM AggregatedOee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.active = TRUE "
            + "AND (:resourceId IS NULL OR o.resourceId IN :resourceId) "
            + "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) "
            + "AND (:workcenterId IS NULL OR o.workcenterId IN :workcenterId) "
            + "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) "
            + "AND (:shopOrderId IS NULL OR o.shopOrderId IN :shopOrderId) "
            + "AND (:item IS NULL OR o.item IN :item) "
            + "AND (:operation IS NULL OR o.operation IN :operation) "
            + "AND o.category IN (:eventTypes) "
            + "AND o.eventSource = :eventSource "

            + "AND ( "
            + "   (:shiftId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND :shopOrderId IS NULL AND :workcenterId IS NULL "
            + "   AND (o.item IS NULL OR o.item = '') "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   AND (o.shopOrderId IS NULL OR o.shopOrderId = '') "
            + "   ) "
            + "   OR (:item IS NOT NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND :shopOrderId IS NULL AND :workcenterId IS NULL AND :shiftId IS NULL "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   AND (o.shopOrderId IS NULL OR o.shopOrderId = '') "
            + "   ) "
            + "   OR (:operation IS NOT NULL AND :item IS NOT NULL AND :batchNumber IS NOT NULL "
            + "   AND :shopOrderId IS NOT NULL "
            + "   AND (o.item IS NOT NULL AND o.item <> '') "
            + "   AND (o.batchNumber IS NOT NULL AND o.batchNumber <> '') "
            + "   AND (o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') "
            + "   ) "
            + "   OR (:shopOrderId IS NOT NULL AND :item IS NULL AND :operation IS NULL AND :batchNumber IS NULL "
            + "   AND (o.item IS NULL OR o.item = '') "
            + "   AND (o.operation IS NULL OR o.operation = '') "
            + "   AND (o.batchNumber IS NULL OR o.batchNumber = '') "
            + "   ) "
            + "   OR ((:site IS NOT NULL OR :workcenterId IS NOT NULL OR :batchNumber IS NOT NULL) "
            + "   AND :item IS NULL AND :operation IS NULL AND :shopOrderId IS NULL AND :shiftId IS NULL "
//            + "   AND (o.batchNumber IS NOT NULL AND o.batchNumber <> '') "
            + "   AND ("
            + "     :checkBatchCondition = false OR ("
            + "       (o.batchNumber IS NOT NULL AND o.batchNumber <> '') AND "
            + "       (o.item IS NOT NULL AND o.item <> '') AND "
            + "       (o.operation IS NOT NULL AND o.operation <> '') AND "
            + "       (o.shopOrderId IS NOT NULL AND o.shopOrderId <> '') "
            + "     )"
            + "   )"
            + "   )) ")
    List<AggregatedOee> findByFilters1(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("resourceId") List<String> resourceId,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterId") List<String> workcenterId,
            @Param("batchNumber") List<String> batchNumber,
            @Param("shopOrderId") List<String> shopOrderId,
            @Param("item") List<String> item,
            @Param("operation") List<String> operation,
            @Param("eventTypes") List<String> eventTypes,
            @Param("eventSource") String eventSource,
            @Param("checkBatchCondition") Boolean checkBatchCondition
    );
}



