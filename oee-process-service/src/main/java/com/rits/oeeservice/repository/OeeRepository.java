package com.rits.oeeservice.repository;

import com.rits.oeeservice.dto.OeeDTO;
import com.rits.oeeservice.dto.OeeWorkCenterDto;
import com.rits.oeeservice.model.Oee;
import com.rits.oeeservice.dto.SpeedLossSummaryDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface OeeRepository extends JpaRepository<Oee,Integer> {
    @Query("SELECT o FROM Oee o WHERE o.site = :site AND o.workcenterId = :workcenterId AND o.resourceId = :resourceId AND o.itemBo = :itemBo")
    Optional<Oee> findOeeData(String site, String workcenterId, String resourceId, String itemBo);



    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime <= :endDateTime "
            + "AND o.intervalEndDateTime >= :startDateTime "
            + "AND (o.batchNumber IS NOT NULL AND o.batchNumber <> '') "
            + "AND o.eventTypeOfPerformance = :eventType "
            + "AND (o.plan > 0 OR o.totalQty > 0) ")
    List<Oee> findBySiteAndIntervalBetween(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("eventType") String eventType);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime <= :endDateTime "
            + "AND o.intervalEndDateTime >= :startDateTime "
            + "AND o.batchNumber IS NOT NULL AND o.batchNumber <> '' "
            + "AND o.eventTypeOfPerformance = :eventType "
            + "AND o.plan > 0 OR o.totalQty>0 ")
    List<Oee> findByIntervalAndSite(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("site") String site,
            @Param("eventType") String eventType);
    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime <= :endDateTime "
            + "AND o.intervalEndDateTime >= :startDateTime "
            + "AND (:machine = false OR (o.batchNumber IS NOT NULL AND o.batchNumber <> '')) "
            + "AND o.eventTypeOfPerformance IN :eventTypes "
            + "AND (o.plan > 0 OR o.totalQty > 0)")
    List<Oee> findByIntervalAndSiteForBatch(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("site") String site,
            @Param("eventTypes") List<String> eventTypes,
            @Param("machine") boolean machine);
    @Query("SELECT o FROM com.rits.oeeservice.model.Oee o " +
            "WHERE o.site = :site " +
            "AND o.intervalStartDateTime <= :endDateTime " +
            "AND o.intervalEndDateTime >= :startDateTime " +
            "AND (o.batchNumber IS NULL OR o.batchNumber = '')")
    List<Oee> findByIntervalAndSite(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("site") String site);







    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.batchNumber IN :batchNumbers "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime")
    List<Oee> findBySiteAndBatchNumberInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("batchNumbers") List<String> batchNumbers,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.batchNumber != '' "
            + "AND (:resourceId IS NULL OR o.resourceId IN :resourceId) "
            + "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) "
            + "AND (:workcenterId IS NULL OR o.workcenterId IN :workcenterId) "
            + "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) "
            + "AND o.eventTypeOfPerformance = :eventType")
    List<Oee> findByFilters(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("resourceId") List<String> resourceId,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterId") List<String> workcenterId,
            @Param("batchNumber") List<String> batchNumber,
            @Param("eventType") String eventType);






    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.workcenterId IN :workcenterIds "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime AND o.batchNumber=''")
    List<Oee> findBySiteAndWorkcenterIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("workcenterIds") List<String> workcenterIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.resourceId IN :resourceIds "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime AND o.batchNumber!=''")
    List<Oee> findBySiteAndResourceIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("resourceIds") List<String> resourceIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.shiftId IN :shiftId "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime AND o.batchNumber!=''")
    List<Oee> findBySiteAndShiftIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("shiftId") List<String> shiftId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.shiftId IN :shiftId "
            + "AND o.workcenterId IN :workcenterIds "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime AND o.batchNumber!=''")
    List<Oee> findBySiteAndShiftIdInAndWorkcenterIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterIds") List<String> workcenterIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.shiftId IN :shiftId "
            + "AND o.workcenterId IN :workcenterIds "
            + "AND o.resourceId IN :resourceIds "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime AND o.batchNumber!=''")
    List<Oee> findBySiteAndShiftIdInAndWorkcenterIdInAndResourceIdInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterIds") List<String> workcenterIds,
            @Param("resourceIds") List<String> resourceIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.shiftId IN :shiftId "
            + "AND o.workcenterId IN :workcenterIds "
            + "AND o.resourceId IN :resourceIds "
            + "AND o.batchNumber IN :batchNumbers "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime")
    List<Oee> findBySiteAndShiftIdInAndWorkcenterIdInAndResourceIdInAndBatchNumberInAndIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("site") String site,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterIds") List<String> workcenterIds,
            @Param("resourceIds") List<String> resourceIds,
            @Param("batchNumbers") List<String> batchNumbers,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT o FROM Oee o "
            + "WHERE o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime")
    List<Oee> findByIntervalStartDateTimeBetweenAndIntervalEndDateTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT r FROM Oee r WHERE r.intervalStartDateTime BETWEEN :startDate AND :endDate " +
            "AND r.intervalEndDateTime BETWEEN :startDate AND :endDate " +
            "AND r.site = :site")
    List<Oee> findByIntervalStartDateTimeAndIntervalEndDateTimeBetweenAndSite(
            LocalDateTime startDate, LocalDateTime endDate, String site);
    @Query("SELECT new com.rits.oeeservice.dto.SpeedLossSummaryDTO(" +
            "o.resourceId, " +
            "(SUM(CASE WHEN o.productionTime > o.actualTime THEN o.productionTime ELSE 0 END) - " +
            "SUM(CASE WHEN o.productionTime > o.actualTime THEN o.actualTime ELSE 0 END))) " +
            "FROM Oee o " +
            "WHERE o.site = :site " +
            "AND o.resourceId IS NOT NULL " +
            "AND o.plan > 0 OR o.totalQty>0 " +
            "AND o.intervalStartDateTime >= :start " +
            "AND o.intervalEndDateTime <= :end " +
            "GROUP BY o.resourceId")
    List<SpeedLossSummaryDTO> findSpeedLossBySiteAndInterval(
            @Param("site") String site,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT new com.rits.oeeservice.dto.SpeedLossSummaryDTO(" +
            "o.resourceId, " +
            "(SUM(CASE WHEN o.productionTime > o.actualTime THEN o.productionTime ELSE 0 END) - " +
            "SUM(CASE WHEN o.productionTime > o.actualTime THEN o.actualTime ELSE 0 END))) " +
            "FROM Oee o " +
            "WHERE o.site = :site " +
            "AND o.resourceId IS NOT NULL " +
            "AND o.plan <> 0 " +
            "AND o.resourceId IN :resourceIds " +
            "AND o.intervalStartDateTime >= :start " +
            "AND o.intervalEndDateTime <= :end " +
            "GROUP BY o.resourceId")
    List<SpeedLossSummaryDTO> findSpeedLossBySiteAndResourceAndInterval(
            @Param("site") String site,
            @Param("resourceIds") List<String> resourceIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime <= :endDateTime "
            + "AND o.intervalEndDateTime >= :startDateTime "
            + "AND (:machine = false OR (o.batchNumber IS NOT NULL AND o.batchNumber <> '')) "
            + "AND o.eventTypeOfPerformance IN (:eventTypes) "
            + "AND (o.plan > 0 OR o.totalQty > 0) ")
    List<Oee> findBySiteAndIntervalBetween(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("eventTypes") List<String> eventTypes,
            @Param("machine") boolean machine);


    @Query("SELECT o FROM Oee o WHERE o.site = :site "
            + "AND o.intervalStartDateTime BETWEEN :startDateTime AND :endDateTime "
            + "AND o.intervalEndDateTime BETWEEN :startDateTime AND :endDateTime "
//            + "AND o.batchNumber != '' "
            + "AND (:resourceId IS NULL OR o.resourceId IN :resourceId) "
            + "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) "
            + "AND (:workcenterId IS NULL OR o.workcenterId IN :workcenterId) "
            + "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) "
            + "AND o.eventTypeOfPerformance IN (:eventTypes)")
    List<Oee> findByFilters(
            @Param("site") String site,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("resourceId") List<String> resourceId,
            @Param("shiftId") List<String> shiftId,
            @Param("workcenterId") List<String> workcenterId,
            @Param("batchNumber") List<String> batchNumber,
            @Param("eventTypes") List<String> eventTypes);


//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.oee AS numeric), 2) AS oee, " +
//            "    o.good_qty, " +
//            "    o.bad_qty, " +
//            "    o.plan, " +
//            "    o.total_downtime, " +
//            "    o.item, " +
//            "    o.operation, " +
//            "    o.shift_id, " +
//            "    o.resource_id, " +
//            "    o.workcenter_id, " +
//            "    o.batch_number " +
//            "FROM public.r_oee o " +
//            "WHERE o.active = true " +
//            "AND o.category = CASE " +
//            "    WHEN :resource IS NOT NULL THEN 'RESOURCE' " +
//            "    ELSE 'WORKCENTER' " +
//            "END " +
//            "AND o.site = :site " +
//            "AND (:batchNumber IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
//            "AND (:item IS NULL OR o.item IN (:item)) " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND (:shoporderId IS NULL OR o.shoporder_id IN (:shoporderId)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND o.plan > 0 " +
//            "AND o.batch_number IS NOT NULL " +
//            "AND o.batch_number <> '' " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime",
//            nativeQuery = true)
//    List<Oee> findOeeData(
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

    @Query("SELECT new com.rits.oeeservice.dto.OeeDTO( " +
            "o.oee, " +
            "o.goodQty, " +
            "o.badQty, " +
            "o.plan, " +
            "o.totalDowntime, " +
            "o.item, " +
            "o.operation, " +
            "o.shiftId, " +
            "o.resourceId, " +
            "o.workcenterId, " +
            "o.batchNumber) " +
            "FROM Oee o " +
            "WHERE o.active = 1 " +
            "AND o.category = CASE " +
            "    WHEN :resource IS NOT NULL THEN 'RESOURCE' " +
            "    ELSE 'WORKCENTER' " +
            "END " +
            "AND o.site = :site " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN (:batchNumber)) " +
            "AND (:operation IS NULL OR o.operation IN (:operation)) " +
            "AND (:item IS NULL OR o.item IN (:item)) " +
            "AND (:resource IS NULL OR o.resourceId IN (:resource)) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN (:workCenter)) " +
            "AND (:shoporderId IS NULL OR o.shoporderId IN (:shoporderId)) " +
            "AND (:shiftId IS NULL OR o.shiftId IN (:shiftId)) " +
            "AND o.plan > 0 " +
            "AND o.batchNumber IS NOT NULL " +
            "AND o.batchNumber <> '' " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime"
    )
    List<OeeDTO> findOeeData(
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
//            "    o.workcenter_id, " +
//            "    ROUND(AVG(CAST(o.availability AS numeric)), 2) AS avg_availability, " +
//            "    ROUND(AVG(CAST(o.oee AS numeric)), 2) AS avg_oee, " +
//            "    ROUND(AVG(CAST(o.performance AS numeric)), 2) AS avg_performance, " +
//            "    ROUND(AVG(CAST(o.quality AS numeric)), 2) AS avg_quality " +
//            "FROM public.r_oee o " +
//            "WHERE o.active = 1 " +
//            "AND (:batchNumber::TEXT IS NULL OR o.batch_number IN (:batchNumber)) " +
//            "AND (:item::TEXT IS NULL OR o.item IN (:item)) " +
//            "AND (:operation::TEXT IS NULL OR o.operation IN (:operation)) " +
//            "AND (:resource::TEXT IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter::TEXT IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND (:shoporderId::TEXT IS NULL OR o.shoporder_id IN (:shoporderId)) " +
//            "AND (:shiftId::TEXT IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND o.site = :site " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "GROUP BY o.workcenter_id",
//            nativeQuery = true)
//    List<Oee> findWorkcenterData(
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

    @Query(value = "SELECT new com.rits.oeeservice.dto.OeeWorkCenterDto( " +
            "    o.workcenterId, " +
            "    AVG(o.availability), " +  // Use AVG for aggregated numeric fields
            "    AVG(o.oee), " +
            "    AVG(o.performance), " +
            "    AVG(o.quality)) " +
            "FROM Oee o " +
            "WHERE o.active = 1 " +
            "AND (:batchNumber IS NULL OR o.batchNumber IN :batchNumber) " +
            "AND (:item IS NULL OR o.item IN :item) " +
            "AND (:operation IS NULL OR o.operation IN :operation) " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND (:shoporderId IS NULL OR o.shoporderId IN :shoporderId) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND o.site = :site " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "GROUP BY o.workcenterId")
    List<OeeWorkCenterDto> findWorkcenterData(
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