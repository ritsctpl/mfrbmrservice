package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.dto.*;
import com.rits.overallequipmentefficiency.model.AggregatedTimePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import javax.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;

public interface AggregatedTimePeriodRepository extends JpaRepository<AggregatedTimePeriod, Long> {

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.category = 'DAY' " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month")
    List<AggregatedTimePeriod> findForMonthAggregation(@Param("site") String site,
                                                       @Param("workcenterId") String workcenterId,
                                                       @Param("year") int year,
                                                       @Param("month") int month);

    AggregatedTimePeriod findBySiteAndWorkcenterIdAndLogDateAndCategory(String site,
                                                                        String workcenterId,
                                                                        LocalDate logDate,
                                                                        String category);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'SHIFT' " +
            "  AND a.active = true " +
            "  AND a.logDate = :logDate")
    List<AggregatedTimePeriod> findForDayAggregationByShift(@Param("site") String site,
                                                            @Param("logDate") LocalDate logDate);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'DAY' " +
            "  AND a.active = true " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month")
    List<AggregatedTimePeriod> findForMonthAggregation(@Param("site") String site,
                                                       @Param("year") int year,
                                                       @Param("month") int month);

/*    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.logDate = :logDate AND a.category = 'DAY'")
    void deactivateDayRecord(@Param("site") String site,
                             @Param("logDate") LocalDate logDate);*/
@Modifying
@Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
        "WHERE a.site = :site " +
        "AND a.logDate = :logDate " +
        "AND a.day = :day " +
        "AND a.month = :month " +
        "AND a.year = :year " +
        "AND a.category = 'DAY'")
void deactivateDayRecord(@Param("site") String site,
                         @Param("logDate") LocalDate logDate,
                         @Param("day") int day,
                         @Param("month") int month,
                         @Param("year") int year);

  /*  @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.logDate = :logDate AND a.category = 'SHIFT' AND a.shiftId = :shiftId")
    void deactivateShiftRecord(@Param("site") String site,
                               @Param("logDate") LocalDate logDate,
                               @Param("shiftId") String shiftId);*/
  @Modifying
  @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
          "WHERE a.site = :site " +
          "AND a.logDate = :logDate " +
          "AND a.day = :day " +
          "AND a.month = :month " +
          "AND a.year = :year " +
          "AND a.category = 'SHIFT' " +
          "AND a.shiftId = :shiftId")
  void deactivateShiftRecord(@Param("site") String site,
                             @Param("logDate") LocalDate logDate,
                             @Param("day") Integer day,
                             @Param("month") Integer month,
                             @Param("year") Integer year,
                             @Param("shiftId") String shiftId);


    /*@Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.logDate = :monthDate AND a.category = 'MONTH'")
    void deactivateMonthRecord(@Param("site") String site,
                               @Param("monthDate") LocalDate monthDate);*/

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.month = :month AND a.year = :year AND a.category = 'MONTH'")
    void deactivateMonthRecord(@Param("site") String site,
                               @Param("month") int month,
                               @Param("year") int year);


    /*@Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.logDate = :logDate AND a.category = 'WORKCENTER_SHIFT' AND a.workcenterId = :workcenterId AND a.shiftId = :shiftId")
    void deactivateWorkcenterIdAndShiftRecord(@Param("site") String site,
                                              @Param("logDate") LocalDate logDate,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("shiftId") String shiftId);*/
    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.logDate = :logDate " +
            "AND a.day = :day " +
            "AND a.month = :month " +
            "AND a.year = :year " +
            "AND a.category = 'WORKCENTER_SHIFT' " +
            "AND a.workcenterId = :workcenterId " +
            "AND a.shiftId = :shiftId")
    void deactivateWorkcenterIdAndShiftRecord(@Param("site") String site,
                                              @Param("logDate") LocalDate logDate,
                                              @Param("day") Integer day,
                                              @Param("month") Integer month,
                                              @Param("year") Integer year,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("shiftId") String shiftId);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_SHIFT' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.logDate = :logDate")
    List<AggregatedTimePeriod> findForDayAggregationByWorkcenterIdAndShift(@Param("site") String site,
                                                                           @Param("workcenterId") String workcenterId,
                                                                           @Param("logDate") LocalDate logDate);

   /* @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.workcenterId = :workcenterId AND a.logDate = :logDate AND a.category = 'WORKCENTER_DAY'")
    void deactivateWorkcenterIdAndDayRecord(@Param("site") String site,
                                            @Param("workcenterId") String workcenterId,
                                            @Param("logDate") LocalDate logDate);*/

  @Modifying
  @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
          "WHERE a.site = :site " +
          "AND a.workcenterId = :workcenterId " +
          "AND a.logDate = :logDate " +
          "AND a.day = :day " +
          "AND a.month = :month " +
          "AND a.year = :year " +
          "AND a.category = 'WORKCENTER_DAY'")
  void deactivateWorkcenterIdAndDayRecord(@Param("site") String site,
                                          @Param("workcenterId") String workcenterId,
                                          @Param("logDate") LocalDate logDate,
                                          @Param("day") int day,
                                          @Param("month") int month,
                                          @Param("year") int year);

  @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_DAY' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month")
    List<AggregatedTimePeriod> findForMonthAggregationByWorkcenterId(@Param("site") String site,
                                                                     @Param("workcenterId") String workcenterId,
                                                                     @Param("year") int year,
                                                                     @Param("month") int month);

   /* @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.workcenterId = :workcenterId AND a.logDate = :monthDate AND a.category = 'WORKCENTER_MONTH'")
    void deactivateWorkcenterIdAndMonthRecord(@Param("site") String site,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("monthDate") LocalDate monthDate);*/
   @Modifying
   @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
           "WHERE a.site = :site " +
           "AND a.workcenterId = :workcenterId " +
           "AND a.month = :month " +
           "AND a.year = :year " +
           "AND a.category = 'WORKCENTER_MONTH'")
   void deactivateWorkcenterIdAndMonthRecord(@Param("site") String site,
                                             @Param("workcenterId") String workcenterId,
                                             @Param("month") int month,
                                             @Param("year") int year);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_MONTH' " +
            "  AND a.active = true " +
            "  AND FUNCTION('YEAR', a.logDate) = :year")
    List<AggregatedTimePeriod> findForYearAggregation(@Param("site") String site,
                                                      @Param("year") int year);

    /*@Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.logDate = :yearDate AND a.category = 'YEAR'")
    void deactivateYearRecord(@Param("site") String site,
                              @Param("yearDate") LocalDate yearDate);*/

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.year = :year AND a.category = 'YEAR'")
    void deactivateYearRecord(@Param("site") String site,
                              @Param("year") int year);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_MONTH' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year")
    List<AggregatedTimePeriod> findForYearAggregationByWorkcenterId(@Param("site") String site,
                                                                    @Param("workcenterId") String workcenterId,
                                                                    @Param("year") int year);

    /*@Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.workcenterId = :workcenterId AND a.logDate = :yearDate AND a.category = 'WORKCENTER_YEAR'")
    void deactivateWorkcenterIdAndYearRecord(@Param("site") String site,
                                             @Param("workcenterId") String workcenterId,
                                             @Param("yearDate") LocalDate yearDate);*/

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site AND a.workcenterId = :workcenterId AND a.year = :year AND a.category = 'WORKCENTER_YEAR'")
    void deactivateWorkcenterIdAndYearRecord(@Param("site") String site,
                                             @Param("workcenterId") String workcenterId,
                                             @Param("year") int year);


//    @Query(value = "SELECT " +
//            "    AVG(CAST(o.oee AS NUMERIC(10,2))) AS oee, " +
//            "    AVG(CAST(o.availability AS NUMERIC(10,2))) AS availability, " +
//            "    AVG(CAST(o.performance AS NUMERIC(10,2))) AS performance, " +
//            "    AVG(CAST(o.quality AS NUMERIC(10,2))) AS quality " +
//            "FROM r_aggregated_time_period o " +
//            "WHERE o.site = :site " +
//            "AND o.active = true " +
//            "AND o.category IN (:category)",
//            nativeQuery = true)
//    List<AggregatedTimePeriod> findTimeOverallData(
//            @Param("site") String site,
//            @Param("category") List<String> category);

    @Query(value = "SELECT new com.rits.overallequipmentefficiency.dto.AggregatedTimePeriodDto( " +
            "    o.oee, " +
            "    o.availability , " +
            "    o.performance , " +
            "    o.quality ) " +
            "FROM AggregatedTimePeriod o " +
            "WHERE o.site = :site " +
            "AND o.active = true " +
            "AND o.category IN (:category)")
    List<AggregatedTimePeriodDto> findTimeOverallData(
            @Param("site") String site,
            @Param("category") List<String> category);

//    @Query(value = "SELECT " +
//            "    CASE " +
//            "        WHEN o.category = 'WORKCENTER_MONTH' THEN 'MONTH' " +
//            "        WHEN o.category = 'WORKCENTER_DAY' THEN 'DAY' " +
//            "        WHEN o.category = 'WORKCENTER_YEAR' THEN 'YEAR' " +
//            "        ELSE 'UNKNOWN' " +
//            "    END AS category_type, " +
//            "    ROUND(CAST(o.oee AS numeric), 2) AS oee, " +
//            "    ROUND(CAST(o.performance AS numeric), 2) AS performance, " +
//            "    ROUND(CAST(o.quality AS numeric), 2) AS quality, " +
//            "    ROUND(CAST(o.availability AS numeric), 2) AS availability " +
//            "FROM r_aggregated_time_period o " +
//            "WHERE o.site = :site " +
//            "AND o.active = true " +
//            "AND o.category IN (:category)",
//            nativeQuery = true)
//    List<AggregatedTimePeriod> findTimeGraphData(
//            @Param("site") String site,
//            @Param("category") List<String> category);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.AggregatedTimePeriodGraphDto( " +
            "    CASE " +
            "        WHEN o.category = 'WORKCENTER_MONTH' THEN 'MONTH' " +
            "        WHEN o.category = 'WORKCENTER_DAY' THEN 'DAY' " +
            "        WHEN o.category = 'WORKCENTER_YEAR' THEN 'YEAR' " +
            "        ELSE 'UNKNOWN' " +
            "    END, " +
            "    o.oee, " +
            "    o.performance, " +
            "    o.quality, " +
            "    o.availability) " +
            "FROM AggregatedTimePeriod o " +
            "WHERE o.site = :site " +
            "AND o.active = true " +
            "AND o.category IN (:category)")
    List<AggregatedTimePeriodGraphDto> findTimeGraphData(
            @Param("site") String site,
            @Param("category") List<String> category);


//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.oee AS NUMERIC), 2) AS oee, " +
//            "    ROUND(CAST(o.availability AS NUMERIC), 2) AS availability, " +
//            "    ROUND(CAST(o.performance AS NUMERIC), 2) AS performance, " +
//            "    ROUND(CAST(o.quality AS NUMERIC), 2) AS quality, " +
//            "    o.total_good_quantity, " +
//            "    o.total_bad_quantity, " +
//            "    o.total_quantity, " +
//            "    o.shift_id " +
//            "FROM r_aggregated_time_period o " +
//            "WHERE o.site = :site " +
//            "AND o.active = true " +
//            "AND o.category = 'DAY'",
//            nativeQuery = true)
//    List<AggregatedTimePeriod> findDayData(@Param("site") String site);

    @Query("SELECT new com.rits.overallequipmentefficiency.dto.DurationAggregatedTimePeriodDto( " +
            "    o.oee, " +
            "    o.availability, " +
            "    o.performance, " +
            "    o.quality, " +
            "    o.totalGoodQuantity, " +
            "    o.totalBadQuantity, " +
            "    o.totalQuantity, " +
            "    o.shiftId) " +
            "FROM AggregatedTimePeriod o " +
            "WHERE o.site = :site " +
            "AND o.active = true " +
            "AND o.category = 'DAY' " +
            "AND (o.logDate >= :startTime) " +
            "AND (o.logDate <= :endTime)")
    List<DurationAggregatedTimePeriodDto> findDayData(
            @Param("site") String site,
            @Param("startTime") LocalDate startTime,
            @Param("endTime") LocalDate endTime);

//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.oee AS NUMERIC), 2) AS oee, " +
//            "    ROUND(CAST(o.availability AS NUMERIC), 2) AS availability, " +
//            "    ROUND(CAST(o.performance AS NUMERIC), 2) AS performance, " +
//            "    ROUND(CAST(o.quality AS NUMERIC), 2) AS quality, " +
//            "    o.total_good_quantity, " +
//            "    o.total_bad_quantity, " +
//            "    o.total_quantity, " +
//            "    o.shift_id " +
//            "FROM r_aggregated_time_period o " +
//            "WHERE o.site = :site " +
//            "AND o.active = true " +
//            "AND o.category = 'MONTH'",
//            nativeQuery = true)
//    List<AggregatedTimePeriod> findMonthData(@Param("site") String site);

    @Query(value = "SELECT new com.rits.overallequipmentefficiency.dto.DurationAggregatedTimePeriodDto( " +
            "    o.oee, " +
            "    o.availability, " +
            "    o.performance, " +
            "    o.quality, " +
            "    o.totalGoodQuantity, " +
            "    o.totalBadQuantity, " +
            "    o.totalQuantity, " +
            "    o.shiftId) " +
            "FROM AggregatedTimePeriod o " +
            "WHERE o.site = :site " +
            "AND o.active = true " +
            "AND o.category = 'MONTH' " +
            "AND (o.logDate >= :startTime) " +
            "AND (o.logDate <= :endTime)")
    List<DurationAggregatedTimePeriodDto> findMonthData(
            @Param("site") String site,
            @Param("startTime") LocalDate startTime,
            @Param("endTime") LocalDate endTime);

//    @Query(value = "SELECT " +
//            "    ROUND(CAST(o.oee AS NUMERIC), 2) AS oee, " +
//            "    ROUND(CAST(o.availability AS NUMERIC), 2) AS availability, " +
//            "    ROUND(CAST(o.performance AS NUMERIC), 2) AS performance, " +
//            "    ROUND(CAST(o.quality AS NUMERIC), 2) AS quality, " +
//            "    o.total_good_quantity, " +
//            "    o.total_bad_quantity, " +
//            "    o.total_quantity, " +
//            "    o.shift_id " +
//            "FROM r_aggregated_time_period o " +
//            "WHERE o.site = :site " +
//            "AND o.active = true " +
//            "AND o.category = 'YEAR'",
//            nativeQuery = true)
//    List<AggregatedTimePeriod> findYearData(@Param("site") String site);

    @Query(value = "SELECT new com.rits.overallequipmentefficiency.dto.DurationAggregatedTimePeriodDto( " +
            "    o.oee, " +
            "    o.availability, " +
            "    o.performance, " +
            "    o.quality, " +
            "    o.totalGoodQuantity, " +
            "    o.totalBadQuantity, " +
            "    o.totalQuantity, " +
            "    o.shiftId) " +
            "FROM AggregatedTimePeriod o " +
            "WHERE o.site = :site " +
            "AND o.active = true " +
            "AND o.category = 'YEAR' " +
            "AND (o.logDate >= :startTime) " +
            "AND (o.logDate <= :endTime)")
    List<DurationAggregatedTimePeriodDto> findYearData(
            @Param("site") String site,
            @Param("startTime") LocalDate startTime,
            @Param("endTime") LocalDate endTime);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.category = 'DAY' " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForMonthAggregation(@Param("site") String site,
                                                       @Param("workcenterId") String workcenterId,
                                                       @Param("year") int year,
                                                       @Param("month") int month,
                                                       @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.month = :month " +
            "AND a.year = :year " +
            "AND a.category = 'MONTH' " +
            "AND a.eventSource = :eventSource")
    void deactivateMonthRecord(@Param("site") String site,
                               @Param("month") int month,
                               @Param("year") int year,
                               @Param("eventSource") String eventSource);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'SHIFT' " +
            "  AND a.active = true " +
            "  AND a.logDate = :logDate " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForDayAggregationByShift(@Param("site") String site,
                                                            @Param("logDate") LocalDate logDate,
                                                            @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'DAY' " +
            "  AND a.active = true " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForMonthAggregation(@Param("site") String site,
                                                       @Param("year") int year,
                                                       @Param("month") int month,
                                                       @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.logDate = :logDate " +
            "  AND a.day = :day " +
            "  AND a.month = :month " +
            "  AND a.year = :year " +
            "  AND a.category = 'DAY' " +
            "  AND a.eventSource = :eventSource")
    void deactivateDayRecord(@Param("site") String site,
                             @Param("logDate") LocalDate logDate,
                             @Param("day") int day,
                             @Param("month") int month,
                             @Param("year") int year,
                             @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.logDate = :logDate " +
            "  AND a.day = :day " +
            "  AND a.month = :month " +
            "  AND a.year = :year " +
            "  AND a.category = 'SHIFT' " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.eventSource = :eventSource")
    void deactivateShiftRecord(@Param("site") String site,
                               @Param("logDate") LocalDate logDate,
                               @Param("day") Integer day,
                               @Param("month") Integer month,
                               @Param("year") Integer year,
                               @Param("shiftId") String shiftId,
                               @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.logDate = :logDate " +
            "  AND a.day = :day " +
            "  AND a.month = :month " +
            "  AND a.year = :year " +
            "  AND a.category = 'WORKCENTER_SHIFT' " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.shiftId = :shiftId " +
            "  AND a.eventSource = :eventSource")
    void deactivateWorkcenterIdAndShiftRecord(@Param("site") String site,
                                              @Param("logDate") LocalDate logDate,
                                              @Param("day") Integer day,
                                              @Param("month") Integer month,
                                              @Param("year") Integer year,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("shiftId") String shiftId,
                                              @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_SHIFT' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.logDate = :logDate " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForDayAggregationByWorkcenterIdAndShift(@Param("site") String site,
                                                                           @Param("workcenterId") String workcenterId,
                                                                           @Param("logDate") LocalDate logDate,
                                                                           @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.logDate = :logDate " +
            "  AND a.day = :day " +
            "  AND a.month = :month " +
            "  AND a.year = :year " +
            "  AND a.category = 'WORKCENTER_DAY' " +
            "  AND a.eventSource = :eventSource")
    void deactivateWorkcenterIdAndDayRecord(@Param("site") String site,
                                            @Param("workcenterId") String workcenterId,
                                            @Param("logDate") LocalDate logDate,
                                            @Param("day") int day,
                                            @Param("month") int month,
                                            @Param("year") int year,
                                            @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_DAY' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND FUNCTION('MONTH', a.logDate) = :month " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForMonthAggregationByWorkcenterId(@Param("site") String site,
                                                                     @Param("workcenterId") String workcenterId,
                                                                     @Param("year") int year,
                                                                     @Param("month") int month,
                                                                     @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND a.month = :month " +
            "  AND a.year = :year " +
            "  AND a.category = 'WORKCENTER_MONTH' " +
            "  AND a.eventSource = :eventSource")
    void deactivateWorkcenterIdAndMonthRecord(@Param("site") String site,
                                              @Param("workcenterId") String workcenterId,
                                              @Param("month") int month,
                                              @Param("year") int year,
                                              @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_MONTH' " +
            "  AND a.active = true " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForYearAggregation(@Param("site") String site,
                                                      @Param("year") int year,
                                                      @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "  AND a.year = :year " +
            "  AND a.category = 'YEAR' " +
            "  AND a.eventSource = :eventSource")
    void deactivateYearRecord(@Param("site") String site,
                              @Param("year") int year,
                              @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "  AND a.category = 'WORKCENTER_MONTH' " +
            "  AND a.active = true " +
            "  AND a.workcenterId = :workcenterId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "  AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForYearAggregationByWorkcenterId(@Param("site") String site,
                                                                    @Param("workcenterId") String workcenterId,
                                                                    @Param("year") int year,
                                                                    @Param("eventSource") String eventSource);



    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.workcenterId = :workcenterId " +
            "AND a.year = :year " +
            "AND a.category = 'WORKCENTER_YEAR' " +
            "AND a.eventSource = :eventSource")
    void deactivateWorkcenterIdAndYearRecord(@Param("site") String site,
                                             @Param("workcenterId") String workcenterId,
                                             @Param("year") int year,
                                             @Param("eventSource") String eventSource);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AggregatedTimePeriod a " +
            "where a.site = :site and a.logDate = :logDate and a.day = :day " +
            "and a.month = :month and a.year = :year " +
            "and a.category = 'DAY' and a.eventSource = :eventSource and a.active = true")
    List<AggregatedTimePeriod> findActiveDayRecordsForLock(@Param("site") String site,
                                                           @Param("logDate") LocalDate logDate,
                                                           @Param("day") int day,
                                                           @Param("month") int month,
                                                           @Param("year") int year,
                                                           @Param("eventSource") String eventSource);



    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_SHIFT' " +
            "AND a.workcenterId = :cell " +
            "AND a.logDate = :logDate " +
            "AND a.day = :day " +
            "AND a.month = :month " +
            "AND a.year = :year " +
            "  AND a.shiftId = :shiftId " +
            "AND a.eventSource = :eventSource")
    void deactivateCellShiftRecord(@Param("site") String site,
                                   @Param("cell") String cell,
                                   @Param("logDate") LocalDate logDate,
                                   @Param("day") int day,
                                   @Param("month") int month,
                                   @Param("year") int year,
                                   @Param("shiftId") String shiftId,
                                   @Param("eventSource") String eventSource);

    // For CELL_SHIFT Day aggregation
    @Query("SELECT a FROM AggregatedTimePeriod a " +
            " WHERE a.site = :site " +
            " AND a.category = 'CELL_SHIFT' " +
            " AND a.active = true " +
            " AND a.workcenterId = :cell " +
            " AND a.logDate = :logDate " +
            " AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellShiftDayAggregation(@Param("site") String site,
                                                              @Param("cell") String cell,
                                                              @Param("logDate") LocalDate logDate,
                                                              @Param("eventSource") String eventSource);

    // For CELL_DAY deactivation:
    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            " WHERE a.site = :site " +
            " AND a.category = 'CELL_DAY' " +
            " AND a.workcenterId = :cell " +
            " AND a.logDate = :logDate " +
            " AND a.day = :day " +
            " AND a.month = :month " +
            " AND a.year = :year " +
            " AND a.eventSource = :eventSource")
    void deactivateCellDayRecord(@Param("site") String site,
                                 @Param("cell") String cell,
                                 @Param("logDate") LocalDate logDate,
                                 @Param("day") int day,
                                 @Param("month") int month,
                                 @Param("year") int year,
                                 @Param("eventSource") String eventSource);


    // For CELL_DAY to CELL_MONTH aggregation
    @Query("SELECT a FROM AggregatedTimePeriod a " +
            " WHERE a.site = :site " +
            " AND a.category = 'CELL_DAY' " +
            " AND a.active = true " +
            " AND a.workcenterId = :cell " +
            " AND FUNCTION('YEAR', a.logDate) = :year " +
            " AND FUNCTION('MONTH', a.logDate) = :month " +
            " AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellMonthAggregation(@Param("site") String site,
                                                           @Param("cell") String cell,
                                                           @Param("year") int year,
                                                           @Param("month") int month,
                                                           @Param("eventSource") String eventSource);

    // For deactivation of CELL_MONTH records
    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            " WHERE a.site = :site " +
            " AND a.category = 'CELL_MONTH' " +
            " AND a.active = true " +
            " AND a.workcenterId = :cell " +
            " AND a.month = :month " +
            " AND a.year = :year " +
            " AND a.eventSource = :eventSource")
    void deactivateCellMonthRecord(@Param("site") String site,
                                   @Param("cell") String cell,
                                   @Param("month") int month,
                                   @Param("year") int year,
                                   @Param("eventSource") String eventSource);



    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            " AND a.active = true " +
            "AND a.category = 'CELL_MONTH' " +
            "AND a.workcenterId = :cellId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellYearAggregation(@Param("site") String site,
                                                          @Param("cellId") String cellId,
                                                          @Param("year") int year,
                                                          @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_YEAR' " +
            "AND a.year = :year " +
            "AND a.workcenterId = :cellId " +
            "AND a.eventSource = :eventSource")
    void deactivateCellYearRecords(@Param("site") String site,
                                   @Param("year") int year,
                                   @Param("cellId") String cellId,
                                   @Param("eventSource") String eventSource);

    // --- Methods for CELL GROUP-level aggregation ---

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_SHIFT' " +
            "AND a.active = true " +
            "AND a.logDate = :logDate " +
            "AND a.eventSource = :eventSource " +
            "  AND a.shiftId = :shiftId " +
            "AND a.workcenterId IN :cellIds")
    List<AggregatedTimePeriod> findForCellGroupShiftAggregation(@Param("site") String site,
                                                                @Param("shiftId") String shiftId,
                                                                @Param("logDate") LocalDate logDate,
                                                                @Param("cellIds") List<String> cellIds,
                                                                @Param("eventSource") String eventSource);


    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_GROUP_SHIFT' " +
            "AND a.workcenterId = :cellGroup " +
            "AND a.logDate = :logDate " +
            "AND a.day = :day " +
            "AND a.month = :month " +
            "AND a.year = :year " +
            "  AND a.shiftId = :shiftId " +
            "AND a.eventSource = :eventSource")
    void deactivateCellGroupShiftRecord(@Param("site") String site,
                                        @Param("cellGroup") String cellGroup,
                                        @Param("logDate") LocalDate logDate,
                                        @Param("day") int day,
                                        @Param("month") int month,
                                        @Param("year") int year,
                                        @Param("shiftId") String shiftId,
                                        @Param("eventSource") String eventSource);


    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_GROUP_SHIFT' " +
            " AND a.active = true " +
            "AND a.logDate = :logDate " +
            "AND a.workcenterId = :cellGroupId " +
            "AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellGroupDayAggregation(@Param("site") String site,
                                                              @Param("cellGroupId") String cellGroupId,
                                                              @Param("logDate") LocalDate logDate,
                                                              @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.logDate = :logDate " +
            "AND a.category = 'CELL_GROUP_DAY' " +
            "AND a.workcenterId = :cellGroupId " +
            " AND a.logDate = :logDate " +
            " AND a.day = :day " +
            " AND a.month = :month " +
            " AND a.year = :year " +
            "AND a.eventSource = :eventSource")
    void deactivateCellGroupDayRecords(@Param("site") String site,
                                       @Param("logDate") LocalDate logDate,
                                       @Param("cellGroupId") String cellGroupId,
                                       @Param("day") int day,
                                       @Param("month") int month,
                                       @Param("year") int year,
                                       @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_GROUP_DAY' " +
            " AND a.active = true " +
            "AND a.workcenterId = :cellGroupId " +
            "AND FUNCTION('YEAR', a.logDate) = :year " +
            " AND FUNCTION('MONTH', a.logDate) = :month " +
            "AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellGroupMonthAggregation(@Param("site") String site,
                                                                @Param("cellGroupId") String cellGroupId,
                                                                @Param("year") int year,
                                                                @Param("month") int month,
                                                                @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_GROUP_MONTH' " +
            " AND a.active = true " +
            "AND a.month = :month " +
            "AND a.year = :year " +
            "AND a.workcenterId = :cellGroupId " +
            "AND a.eventSource = :eventSource")
    void deactivateCellGroupMonthRecords(@Param("site") String site,
                                         @Param("month") int month,
                                         @Param("year") int year,
                                         @Param("cellGroupId") String cellGroupId,
                                         @Param("eventSource") String eventSource);

    @Query("SELECT a FROM AggregatedTimePeriod a " +
            "WHERE a.site = :site " +
            " AND a.active = true " +
            "AND a.category = 'CELL_GROUP_MONTH' " +
            "AND a.workcenterId = :cellGroupId " +
            "  AND FUNCTION('YEAR', a.logDate) = :year " +
            "AND a.eventSource = :eventSource")
    List<AggregatedTimePeriod> findForCellGroupYearAggregation(@Param("site") String site,
                                                               @Param("cellGroupId") String cellGroupId,
                                                               @Param("year") int year,
                                                               @Param("eventSource") String eventSource);

    @Modifying
    @Query("UPDATE AggregatedTimePeriod a SET a.active = false " +
            "WHERE a.site = :site " +
            "AND a.category = 'CELL_GROUP_YEAR' " +
            "AND a.year = :year " +
            "AND a.workcenterId = :cellGroupId " +
            "AND a.eventSource = :eventSource")
    void deactivateCellGroupYearRecords(@Param("site") String site,
                                        @Param("year") int year,
                                        @Param("cellGroupId") String cellGroupId,
                                        @Param("eventSource") String eventSource);


}

