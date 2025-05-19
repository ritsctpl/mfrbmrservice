package com.rits.overallequipmentefficiency.repository;


import com.rits.overallequipmentefficiency.model.AvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public interface  AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {


    // Find all availability records for a given site and shift
    List<AvailabilityEntity> findBySiteAndShiftIdAndAvailabilityDate(
            String site, String shiftId, LocalDate shiftDate);


    // Find by site, shift, and resource
    List<AvailabilityEntity> findBySiteAndShiftIdAndResourceIdAndAvailabilityDate(
            String site, String shiftId, String resourceId, LocalDate availabilityDate);

    // Find by site, shift, and workcenter
    List<AvailabilityEntity> findBySiteAndShiftIdAndWorkcenterIdAndAvailabilityDate(
            String site, String shiftId, String workcenterId, LocalDate availabilityDate);

    // Finds all AvailabilityEntity records for a given shiftId where:
    //   shiftStartDateTime >= :start AND intervalEndDateTime <= :end.
    @Query("SELECT a FROM AvailabilityEntity a " +
            "WHERE a.shiftId = :shiftId " +
            "AND a.shiftStartDateTime >= :start " +
            "AND a.intervalEndDateTime <= :end")
    List<AvailabilityEntity> findByShiftIdAndIntervalBetween(@Param("shiftId") String shiftId,
                                                             @Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end);



    // If the aggregation is by resource, search by resourceId.
    @Query("SELECT a FROM AvailabilityEntity a " +
            "WHERE a.site = :site " +
            "AND a.shiftId = :shiftId " +
            "AND a.availabilityDate = :availabilityDate " +
            "AND a.resourceId = :resourceId " +
            "AND a.intervalStartDateTime >= :start " +
            "AND a.intervalEndDateTime <= :end")
    List<AvailabilityEntity> findByCriteriaAndInterval(
            @Param("site") String site,
            @Param("shiftId") String shiftId,
            @Param("availabilityDate") LocalDate availabilityDate,
            @Param("resourceId") String resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // If the aggregation is by workcenter, search by workcenterId.
    @Query("SELECT a FROM AvailabilityEntity a " +
            "WHERE a.site = :site " +
            "AND a.shiftId = :shiftId " +
            "AND a.availabilityDate = :availabilityDate " +
            "AND a.workcenterId = :workcenterId " +
            "AND a.intervalStartDateTime >= :start " +
            "AND a.intervalEndDateTime <= :end")
    List<AvailabilityEntity> findByCriteriaAndIntervalForWorkcenter(
            @Param("site") String site,
            @Param("shiftId") String shiftId,
            @Param("availabilityDate") LocalDate availabilityDate,
            @Param("workcenterId") String workcenterId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}


