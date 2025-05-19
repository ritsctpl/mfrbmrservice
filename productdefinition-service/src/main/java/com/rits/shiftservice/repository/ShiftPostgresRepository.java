package com.rits.shiftservice.repository;

import com.rits.shiftservice.dto.BreakMinutes;
import com.rits.shiftservice.dto.ShiftDetailsDTO;
import com.rits.shiftservice.dto.ShiftResponse;
import com.rits.shiftservice.model.Break;
import com.rits.shiftservice.model.Shift;
import com.rits.shiftservice.model.ShiftIntervals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ShiftPostgresRepository extends JpaRepository<Shift,Integer> {

    @Query("SELECT s FROM Shift s WHERE s.site = :site AND s.resourceId = :resourceId AND s.shiftType = 'Resource' and s.active='1'")
    List<Shift> findActiveShiftByResourceId(@Param("site") String site, @Param("resourceId") String resourceId);

    @Query("SELECT s FROM Shift s WHERE s.site = :site AND s.workCenterId = :workCenterId AND s.shiftType = 'WorkCenter' and s.active='1'")
    List<Shift> findActiveShiftByWorkcenterId(@Param("site") String site, @Param("workCenterId") String workCenterId);

    @Query("SELECT s FROM Shift s WHERE s.site = :site AND s.shiftType = 'General' and s.active='1'")
    List<Shift> findActiveShiftByGeneral(@Param("site") String site);
    List<Shift> findByHandleAndActive(String handle ,int active);

    List<Shift> findBySiteAndShiftIdAndAndShiftTypeAndActive(String site, String shiftId, String shiftType, int active);
    List<Shift> findBySiteAndShiftIdAndAndShiftTypeAndResourceIdAndActive(String site, String shiftId, String shiftType, String resourceId,int active);

    List<Shift> findBySiteAndShiftIdAndAndShiftTypeAndWorkCenterIdAndActive(String site, String shiftId, String shiftType, String workCenterId,int active);
    Shift findBySiteAndActiveAndShiftId(String site,int active, String shiftId);
    List<Shift> findTopBySiteOrderByCreatedDateTimeDesc(String site);
    List<Shift> findByVersion(String version);
    List<ShiftResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);
    List<ShiftResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    @Query(value = "SELECT s.shift_id, s.created_date_time, s.handle, " +
            "COALESCE(MIN(b.break_time_start), '00:00:00') AS break_start_time " +
            "FROM r_shift s " +
            "JOIN r_shift_intervals si ON s.id = si.shift_fk " +
            "LEFT JOIN r_break b ON si.id = b.shift_interval_id " +  // Left join to handle missing breaks
            "WHERE s.active = 1 " +
            "AND si.valid_from <= CURRENT_TIMESTAMP " +
            "AND (si.valid_end IS NULL OR si.valid_end >= CURRENT_TIMESTAMP) " +
            "AND (" +
            "     (CAST(si.start_time AS TIME) <= CAST(si.end_time AS TIME) " +
            "      AND CURRENT_TIME BETWEEN CAST(si.start_time AS TIME) AND CAST(si.end_time AS TIME)) " +
            "     OR " +
            "     (CAST(si.start_time AS TIME) > CAST(si.end_time AS TIME) " +
            "      AND (CURRENT_TIME >= CAST(si.start_time AS TIME) OR CURRENT_TIME <= CAST(si.end_time AS TIME)))" +
            ") " +
            "AND s.site = :site and s.active='1' " +
            "GROUP BY s.shift_id, s.created_date_time, s.handle",
            nativeQuery = true)
    List<Object[]> findCurrentShiftAndBreakData(@Param("site") String site);






    @Query(value = "SELECT s.shift_id, s.shift_type, s.created_date_time, si.start_time, si.end_time, s.handle " +
            "FROM r_shift s " +
            "JOIN r_shift_intervals si ON s.id = si.shift_fk " +
            "WHERE s.site = :site " +
            "AND s.active = 1 " +
            "AND s.shift_type = :shiftType " +
            "AND s.handle = si.shift_ref " +
            "AND DATE(NOW()) BETWEEN DATE(si.valid_from) AND COALESCE(DATE(si.valid_end), DATE(NOW())) " +
            "AND ( " +
            "(CAST(si.start_time AS TIME) <= CURRENT_TIME AND CURRENT_TIME <= CAST(si.end_time AS TIME)) " +
            "OR " +
            "(CAST(si.start_time AS TIME) > CAST(si.end_time AS TIME) AND " +
            "(CURRENT_TIME >= CAST(si.start_time AS TIME) OR CURRENT_TIME <= CAST(si.end_time AS TIME)))" +
            ") " +
            "AND ( " +
            "(:shiftType = 'General' OR (:shiftType = 'Resource' AND s.resource_id = :resourceId) " +
            "OR (:shiftType = 'Workcenter' AND s.work_center_id = :workcenterId)) " +
            ")",
            nativeQuery = true)
    Object[] getCurrentShiftDetailsByShiftType(
            @Param("site") String site,
            @Param("shiftType") String shiftType,
            @Param("resourceId") String resourceId,
            @Param("workcenterId") String workcenterId
    );



    Optional<Shift> findBySiteAndHandle(String site, String handle);

    @Query(value = "SELECT s.id AS shift_id, " +
            "s.site AS shift_site, " +
            "s.handle AS shift_handle, " +
            "si.id AS interval_id, " +
            "si.start_time, si.end_time, si.valid_from, si.valid_end, " +
            "b.id AS break_id, " +
            "b.break_time_start, b.break_time_end " +
            "FROM r_shift s " +
            "JOIN r_shift_intervals si ON s.id = si.shift_fk " +
            "JOIN r_break b ON si.id = b.shift_interval_id " +
            "WHERE s.active = 1 " +
            "AND si.active = 1 " +
            "AND b.active = 1 " +
            "AND s.handle = :shiftRef " +
            "AND s.site = :site " +
            "AND si.valid_from <= :endDate " +
            "AND (si.valid_end >= :startDate OR si.valid_end IS NULL)", nativeQuery = true)
    List<Shift> findShiftWithIntervalsAndBreaks(@Param("shiftRef") String shiftRef,
                                                @Param("site") String site,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);


    @Query(value = "SELECT s.handle, (SUM(si.shift_mean_time) - COALESCE(SUM(b.mean_time), 0)) AS planned_production_time " +
            "FROM r_shift s " +
            "JOIN r_shift_intervals si ON si.shift_fk = s.id " +
            "LEFT JOIN r_break b ON b.shift_interval_id = si.id " +
            "WHERE s.handle IN (:shiftHandles) " +
            "GROUP BY s.handle",
            nativeQuery = true)
    List<Object[]> getPlannedProductionTimes(@Param("shiftHandles") List<String> shiftHandles);

    @Query(value = "SELECT si.valid_from, si.valid_end, si.start_time, si.end_time FROM r_shift s " +
            "JOIN r_shift_intervals si ON s.id = si.shift_fk " +
            "WHERE s.active = 1 " +
            "AND s.shift_type = :shiftType " +
            "AND s.site = :site " +
            "ORDER BY si.valid_from",
            nativeQuery = true)
    Object[] findShiftIntervalsByTypeAndSite(@Param("shiftType") String shiftType, @Param("site") String site);


}