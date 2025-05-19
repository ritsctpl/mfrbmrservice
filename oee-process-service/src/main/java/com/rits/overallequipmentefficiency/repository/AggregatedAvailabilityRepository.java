/*
package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.AggregatedAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rits.overallequipmentefficiency.model.AggregatedAvailability;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AggregatedAvailabilityRepository extends JpaRepository<AggregatedAvailabilityEntity, Long> {

    Optional<AggregatedAvailabilityEntity> findBySiteAndShiftIdAndAggregationLevel(
            String site, String shiftId, String aggregationLevel);

    Optional<AggregatedAvailabilityEntity> findBySiteAndShiftIdAndResourceIdAndWorkcenterId(
            String site, String shiftId, String resourceId, String workcenterId);

    AggregatedAvailability findBySiteAndCategoryAndIdentifierAndAvailabilityDate(
            String site, String category, String identifier, LocalDate availabilityDate);

}

*/
package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.AggregatedAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface AggregatedAvailabilityRepository extends JpaRepository<AggregatedAvailability, Long> {
    AggregatedAvailability findBySiteAndCategoryAndIdentifierAndAvailabilityDate(
            String site, String category, String identifier, LocalDate availabilityDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AggregatedAvailability a " +
            "where a.site = :site and a.category = :category and a.identifier = :identifier " +
            "and a.shiftId = :shiftId and a.availabilityDate = :availabilityDate")
    AggregatedAvailability findForUpdate(@Param("site") String site,
                                         @Param("category") String category,
                                         @Param("identifier") String identifier,
                                         @Param("shiftId") String shiftId,
                                         @Param("availabilityDate") LocalDate availabilityDate);

    AggregatedAvailability findBySiteAndCategoryAndIdentifierAndShiftIdAndAvailabilityDate(
            String site, String category, String identifier, String shiftId, LocalDate availabilityDate);
}
