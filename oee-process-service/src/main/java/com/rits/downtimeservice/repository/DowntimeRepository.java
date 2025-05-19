package com.rits.downtimeservice.repository;

import com.rits.common.dto.OeeFilterRequest;
import com.rits.downtimeservice.dto.DowntTimeByWorkcenter;
import com.rits.downtimeservice.dto.DowntimeByResource;
import com.rits.downtimeservice.dto.DowntimeReasonSummaryDTO;
import com.rits.downtimeservice.model.Downtime;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import org.springframework.lang.Nullable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//@Repository
//public interface MachineLogRepository extends JpaRepository<MachineLogEntity,Long> {
//}
public interface DowntimeRepository extends JpaRepository<Downtime, Long> {

    @Query("SELECT d FROM Downtime d " +
            "WHERE d.resourceId = :resourceId " +
            "AND (:workcenterId IS NULL OR d.workcenterId = :workcenterId) " +
            "AND d.downtEvent = 0 " +
            "AND d.active = 1 " +
            "ORDER BY d.createdDatetime ASC")
    Page<Downtime> findFirstDownEvent(@Param("resourceId") String resourceId,
                                      @Param("workcenterId") String workcenterId,
                                      Pageable pageable);



/*    @Modifying
    @Query("UPDATE Downtime d SET d.downtimeDuration = :downtimeDuration, " +
            "d.plannedOperatingTime = :plannedOperatingTime, " +
            "d.active = 0, " +
            "d.downtimeEnd = :downtimeEnd " +
            "WHERE d.resourceId = :resourceId AND d.workcenterId = :workcenterId AND d.active = 1")
    void updateDowntimeAndDeactivateRecords(@Param("resourceId") String resourceId,
                                            @Param("workcenterId") String workcenterId,
                                            @Param("downtimeEnd") LocalDateTime downtimeEnd,
                                            @Param("downtimeDuration") long downtimeDuration,
                                            @Param("plannedOperatingTime") long plannedOperatingTime)*/;

    @Modifying
    @Query("UPDATE Downtime d SET d.downtimeDuration = :downtimeDuration, " +
            "d.plannedOperatingTime = :plannedOperatingTime, " +
            "d.active = 0, " +
            "d.downtimeEnd = :downtimeEnd " +
            "WHERE d.resourceId = :resourceId " +
            "AND (:workcenterId IS NULL OR d.workcenterId = :workcenterId) " +
            "AND d.active = 1 " +
            "AND d.downtEvent = 0")
    void updateDowntimeAndDeactivateRecords(@Param("resourceId") String resourceId,
                                            @Param("workcenterId") String workcenterId,
                                            @Param("downtimeEnd") LocalDateTime downtimeEnd,
                                            @Param("downtimeDuration") long downtimeDuration,
                                            @Param("plannedOperatingTime") long plannedOperatingTime);
    @Modifying
    @Query("UPDATE Downtime d SET d.active = 0 " +
            "WHERE d.resourceId = :resourceId " +
            "AND (:workcenterId IS NULL OR d.workcenterId = :workcenterId) " +
            "AND d.active = 1 " +
            "AND d.downtEvent = 0")
    void deactivateRecords(@Param("resourceId") String resourceId,
                                            @Param("workcenterId") String workcenterId);

    @Query("SELECT d FROM Downtime d " +
            "WHERE d.resourceId = :resourceId AND d.workcenterId = :workcenterId AND d.active = 1")
    List<Downtime> findActiveDowntimes(@Param("resourceId") String resourceId,
                                       @Param("workcenterId") String workcenterId);


    @Modifying
    @Query("UPDATE Downtime d SET d.active = 0 " +
            "WHERE d.resourceId = :resourceId AND d.workcenterId = :workcenterId AND d.active = 1")
    int closeActiveDowntimes(@Param("resourceId") String resourceId,
                             @Param("workcenterId") String workcenterId);

    List<Downtime> findByResourceIdAndWorkcenterIdAndActive(String resourceId, String workcenterId, Integer Active);

    List<Downtime> findByDowntimeType(String downtimeType);

    Long countByActive(Integer Active);

    List<Downtime> findByRootCauseOrderByDowntimeStartDesc(String rootCause);

    @Query("SELECT d FROM Downtime d WHERE d.downtimeStart BETWEEN :startDate AND :endDate ORDER BY d.downtimeStart DESC")
    List<Downtime> findByDowntimeStartBetweenOrderByDowntimeStartDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<Downtime> findByResourceIdOrderByDowntimeStartDesc(String resourceId);

    List<Downtime> findByWorkcenterIdOrderByDowntimeStartDesc(String workcenterId);

    List<Downtime> findByShiftIdOrderByDowntimeStartDesc(String shiftId);

    List<Downtime> findByReasonOrderByDowntimeStartDesc(String reason);

    @Query("SELECT d FROM Downtime d WHERE d.resourceId = :resourceId AND d.downtimeStart BETWEEN :startDate AND :endDate ORDER BY d.downtimeStart DESC")
    List<Downtime> findByResourceIdAndDowntimeStartBetweenOrderByDowntimeStartDesc(String resourceId, LocalDateTime startDate, LocalDateTime endDate);
    List<Downtime>  findGroupedDowntimesBySiteAndDowntEvent(String site,Integer downEvent);
    @Query("SELECT d FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.resourceId IN :resourceList " +
            "AND d.active = 1 " +
            "AND (:workcenter IS NULL OR d.workcenterId = :workcenter) " +
            "AND (:shift IS NULL OR d.shiftId = :shift) " +
            "ORDER BY d.resourceId, d.workcenterId, d.shiftId, d.site")
    List<Downtime> findDowntimeSummary(
            @Param("site") String site,
            @Param("resourceList") List<String> resourceList,
            @Param("workcenter") String workcenterId,
            @Param("shift") String shiftId
    );

    @Query("SELECT d FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.downtimeStart >= :downtimeStart " +
            "AND d.downtimeStart <= :downtimeEnd " +
            "AND (:resourceList IS NULL OR d.resourceId IN :resourceList) " +
            "ORDER BY d.downtimeStart")
    List<Downtime> findDowntime(
            @Param("site") String site,
            @Param("resourceList") @Nullable List<String> resourceList,
            @Param("downtimeStart") LocalDateTime downtimeStart,
            @Param("downtimeEnd") LocalDateTime downtimeEnd
    );


    @Modifying
    @Transactional
    @Query("UPDATE Downtime d SET d.active = 0 " +
            "WHERE (:workcenterId IS NULL OR d.workcenterId = :workcenterId) " +
            "AND (:resourceId IS NULL OR d.resourceId = :resourceId) " +
            "AND (:shiftId IS NULL OR d.shiftId = :shiftId) " +
            "AND (:site IS NULL OR d.site = :site) " +
            "AND d.downtEvent = 0 " +
            "AND d.createdDatetime BETWEEN :startDatetime AND CURRENT_TIMESTAMP")
    int updateDowntimeStatus(
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("shiftId") String shiftId,
            @Param("site") String site,
            @Param("startDatetime") LocalDateTime startDatetime);

    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND (:#{#request.resourceId} IS NULL OR d.resourceId IN :#{#request.resourceId}) " +
            "AND (:#{#request.workcenterId} IS NULL OR d.workcenterId IN :#{#request.workcenterId}) " +
            "AND (:#{#request.shiftId} IS NULL OR d.shiftId IN :#{#request.shiftId})")
    List<Downtime> findOverallDowntime(OeeFilterRequest request);

    // Query for downtime over time
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.resourceId = :#{#request.resourceId} " +
            "AND d.shiftId IN :#{#request.shiftId}")
    List<Downtime> findDowntimeOverTime(OeeFilterRequest request);

    // Query for downtime by reason
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.reason IN :#{#request.reasonCode} " +
            "AND d.workcenterId IN :#{#request.workcenterId}")
    List<Downtime> findDowntimeByReason(OeeFilterRequest request);

    // Query for downtime by machine
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.resourceId IN :#{#request.resourceId} " +
            "AND d.workcenterId IN :#{#request.workcenterId}")
    List<Downtime> findDowntimeByMachine(OeeFilterRequest request);

    // Query for cumulative downtime
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.resourceId IN :#{#request.resourceId}")
    List<Downtime> findCumulativeDowntime(OeeFilterRequest request);

    // Query for downtime vs production output
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.workcenterId IN :#{#request.workcenterId}")
    List<Downtime> findDowntimeVsProductionOutput(OeeFilterRequest request);

    // Query for downtime impact
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.isOeeImpact = 1")
    List<Downtime> findDowntimeImpact(OeeFilterRequest request);

    // Query for downtime duration distribution
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime})")
    List<Downtime> findDowntimeDurationDistribution(OeeFilterRequest request);

    // Query for downtime analysis
    @Query("SELECT d FROM Downtime d WHERE d.site = :#{#request.site} " +
            "AND (d.downtimeStart BETWEEN :#{#request.startTime} AND :#{#request.endTime}) " +
            "AND d.workcenterId IN :#{#request.workcenterId}")
    List<Downtime> findDowntimeAnalysis(OeeFilterRequest request);
    @Query("SELECT new com.rits.downtimeservice.dto.DowntTimeByWorkcenter(d.workcenterId, SUM(d.downtimeDuration)) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.downtimeStart >= :start AND d.downtimeEnd <= :end " +
            "GROUP BY d.workcenterId")
    List<DowntTimeByWorkcenter> findDowntimeSummaryBySiteAndInterval(@Param("site") String site,
                                                                     @Param("start") LocalDateTime start,
                                                                     @Param("end") LocalDateTime end);

    @Query("SELECT new com.rits.downtimeservice.dto.DowntTimeByWorkcenter(d.workcenterId, SUM(d.downtimeDuration)) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.workcenterId IN :workcenterList " +
            "AND d.downtimeStart >= :start AND d.downtimeEnd <= :end " +
            "GROUP BY d.workcenterId")
    List<DowntTimeByWorkcenter> findDowntimeSummaryBySiteAndWorkcenterAndInterval(@Param("site") String site,
                                                                               @Param("workcenterList") List<String> workcenterList,
                                                                               @Param("start") LocalDateTime start,
                                                                               @Param("end") LocalDateTime end);

    @Query("SELECT new com.rits.downtimeservice.dto.DowntimeByResource(d.resourceId, SUM(d.downtimeDuration)/60) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.downtimeStart >= :start " +
            "AND d.downtimeEnd <= :end " +
            "GROUP BY d.resourceId")
    List<DowntimeByResource> findDowntimeBySiteAndInterval(@Param("site") String site,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);

    @Query("SELECT new com.rits.downtimeservice.dto.DowntimeByResource(d.resourceId, SUM(d.downtimeDuration) / 60) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.workcenterId IN :workcenterList " +
            "AND d.downtimeStart >= :start " +
            "AND d.downtimeEnd <= :end " +
            "GROUP BY d.resourceId")
    List<DowntimeByResource> findDowntimeBySiteAndWorkcenterAndInterval(@Param("site") String site,
                                                                        @Param("workcenterList") List<String> workcenterList,
                                                                        @Param("start") LocalDateTime start,
                                                                        @Param("end") LocalDateTime end);

    @Query("SELECT new com.rits.downtimeservice.dto.DowntimeByResource(d.resourceId, SUM(d.downtimeDuration)/60) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.resourceId IN :resourceList " +
            "AND d.downtimeStart >= :start " +
            "AND d.downtimeEnd <= :end " +
            "GROUP BY d.resourceId")
    List<DowntimeByResource> findDowntimeBySiteAndResourceIdAndInterval(@Param("site") String site,
                                                                        @Param("resourceList") List<String> resourceList,
                                                                        @Param("start") LocalDateTime start,
                                                                        @Param("end") LocalDateTime end);

    @Query("SELECT d FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.downtimeStart >= :start " +
            "AND ( (d.downtimeEnd IS NOT NULL AND d.downtimeEnd <= :end) OR d.downtimeEnd IS NULL )")
    List<Downtime> findDowntimesBySiteAndInterval(@Param("site") String site,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("SELECT d FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.resourceId IN :resourceIds " +
            "AND d.downtimeStart >= :start " +
            "AND ( (d.downtimeEnd IS NOT NULL AND d.downtimeEnd <= :end) OR d.downtimeEnd IS NULL )")
    List<Downtime> findDowntimeBySiteAndResourceAndInterval(@Param("site") String site,
                                                            @Param("resourceIds") List<String> resourceIds,
                                                            @Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end);


    @Query("SELECT new com.rits.downtimeservice.dto.DowntimeReasonSummaryDTO(" +
            "CASE WHEN d.reason IS NULL OR d.reason = '' THEN 'Unknown' ELSE d.reason END, " +
            "SUM(CASE WHEN d.downtimeDuration IS NULL THEN " +
            "FUNCTION('date_part', 'epoch', :now - d.downtimeStart) " +
            "ELSE d.downtimeDuration END)) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.downtimeStart >= :start " +
            "AND (d.downtimeEnd <= :end OR d.downtimeEnd IS NULL) " +
            "GROUP BY CASE WHEN d.reason IS NULL OR d.reason = '' THEN 'Unknown' ELSE d.reason END")
    List<DowntimeReasonSummaryDTO> findDowntimeSummaryBySiteAndInterval(
            @Param("site") String site,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now);


    @Query("SELECT new com.rits.downtimeservice.dto.DowntimeReasonSummaryDTO(" +
            "d.reason, " +
            "SUM(CASE WHEN d.downtimeDuration IS NULL THEN " +
            "FUNCTION('date_part', 'epoch', :now - d.downtimeStart) " +
            "ELSE d.downtimeDuration END)) " +
            "FROM Downtime d " +
            "WHERE d.site = :site " +
            "AND d.resourceId IN :resourceIds " +
            "AND d.downtimeStart >= :start " +
            "AND (d.downtimeEnd <= :end OR d.downtimeEnd IS NULL) " +
            "GROUP BY d.reason")
    List<DowntimeReasonSummaryDTO> findDowntimeSummaryBySiteAndResourceAndInterval(
            @Param("site") String site,
            @Param("resourceIds") List<String> resourceIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now);
    Downtime findFirstBySiteAndResourceIdAndWorkcenterIdAndActiveOrderByCreatedDatetimeDesc(
            String site, String resourceId, String workcenterId, Integer active);
    Downtime findFirstBySiteAndResourceIdAndActiveOrderByCreatedDatetimeDesc(
            String site, String resourceId, Integer active);
}