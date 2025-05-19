package com.rits.availability.repository;

import com.rits.availability.dto.AggregatedAvailabilityDTO;
import com.rits.availability.dto.CombinationRequest;
import com.rits.availability.dto.GraphOeeAvailabilityDTO;
import com.rits.availability.dto.OeeAvailabilityDTO;
import com.rits.availability.model.OeeAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OeeAvailabilityRepository extends JpaRepository<OeeAvailabilityEntity, Long> {

    List<OeeAvailabilityEntity> findByResourceIdInAndAvailabilityDateBetweenOrderByResourceIdAscAvailabilityDateDesc(List<String> resourceId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("DELETE FROM OeeAvailabilityEntity a WHERE a.site = :site AND a.resourceId = :resourceId AND a.workcenterId = :workcenterId")
    void deleteAvailability(@Param("site") String site, @Param("resourceId") String resourceId, @Param("workcenterId") String workcenterId);


    @Query("SELECT new com.rits.availability.dto.AggregatedAvailabilityDTO(a.resourceId, a.workcenterId, a.site, a.shiftRef, " +
            "SUM(a.runtime), AVG(a.availabilityPercentage), SUM(a.actualAvailableTime)) " +
            "FROM OeeAvailabilityEntity a " +
            "WHERE (a.resourceId, a.site, a.shiftRef) IN :combinations " +
            "GROUP BY a.resourceId, a.workcenterId, a.site, a.shiftRef")
    List<AggregatedAvailabilityDTO> findGroupedAvailability(@Param("combinations") List<CombinationRequest> combinations);
    @Query(value = "SELECT resource_id, workcenter_id, site, shift_id, SUM(actual_available_time * 60) AS totalAvailableTimeSeconds " +
            "FROM r_availability " +
            "WHERE availability_date BETWEEN :startDate AND :endDate " +
            "AND site = :site " +
            "AND created_datetime BETWEEN :createdStart AND :createdEnd " +
            "GROUP BY resource_id, workcenter_id, site, shift_id",
            nativeQuery = true)
    List<OeeAvailabilityEntity> findAvailability(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("site") String site,
                                                 @Param("createdStart") LocalDateTime createdStart,
                                                 @Param("createdEnd") LocalDateTime createdEnd);


    @Query("SELECT new com.rits.availability.dto.OeeAvailabilityDTO( " +
            "e.resourceId, " +
            "AVG(e.plannedOperatingTime), " +
            "AVG(e.downtime), " +
            "AVG(e.availabilityPercentage), " +
            "e.workcenterId, " +
            "e.shiftId) " +
            "FROM OeeAvailabilityEntity e " +
            "WHERE e.active = 1 " +
            "AND e.site = :site " +
            "AND (:resource IS NULL OR e.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR e.workcenterId IN :workCenter) " +
            "AND (:shiftId IS NULL OR e.shiftId IN :shiftId) " +
            "AND e.intervalStartDateTime >= :startTime " +
            "AND e.intervalEndDateTime <= :endTime " +
            "AND e.resourceId IS NOT NULL AND e.resourceId <> '' " +
            "GROUP BY e.resourceId, e.workcenterId, e.shiftId")
    List<OeeAvailabilityDTO> findAvailabilityData(
            @Param("site") String site,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

//    @Query(value = "SELECT " +
//            "    o.resource_id, " +
//            "    ROUND(AVG(CAST(o.actual_available_time AS numeric)), 2) AS avg_actual_available_time, " +
//            "    ROUND(AVG(CAST(o.availability_percentage AS numeric)), 2) AS avg_availability_percentage " +
//            "FROM r_availability o " +
//            "WHERE o.active = 1 " +
//            "AND o.site = :site " +
//            "AND (:resource IS NULL OR o.resource_id IN (:resource)) " +
//            "AND (:workCenter IS NULL OR o.workcenter_id IN (:workCenter)) " +
//            "AND (:shiftId IS NULL OR o.shift_id IN (:shiftId)) " +
//            "AND o.interval_start_date_time >= :startTime " +
//            "AND o.interval_end_date_time <= :endTime " +
//            "AND o.resource_id IS NOT NULL AND o.resource_id <> '' " +
//            "GROUP BY o.resource_id",
//            nativeQuery = true)
//    List<OeeAvailabilityEntity> findAvailabilityGraphData(
//            @Param("site") String site,
//            @Param("resource") List<String> resource,
//            @Param("workCenter") List<String> workCenter,
//            @Param("shiftId") List<String> shiftId,
//            @Param("startTime") LocalDateTime startTime,
//            @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT new com.rits.availability.dto.GraphOeeAvailabilityDTO( " +
            "    o.resourceId, " +
            "    o.actualAvailableTime, " +
            "    o.availabilityPercentage) " +
            "FROM OeeAvailabilityEntity o " +
            "WHERE o.active = 1 " +
            "AND o.site = :site " +
            "AND (:resource IS NULL OR o.resourceId IN :resource) " +
            "AND (:workCenter IS NULL OR o.workcenterId IN :workCenter) " +
            "AND (:shiftId IS NULL OR o.shiftId IN :shiftId) " +
            "AND o.intervalStartDateTime >= :startTime " +
            "AND o.intervalEndDateTime <= :endTime " +
            "AND o.resourceId IS NOT NULL AND o.resourceId <> '' " +
            "GROUP BY o.resourceId")
    List<GraphOeeAvailabilityDTO> findAvailabilityGraphData(
            @Param("site") String site,
            @Param("resource") List<String> resource,
            @Param("workCenter") List<String> workCenter,
            @Param("shiftId") List<String> shiftId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}