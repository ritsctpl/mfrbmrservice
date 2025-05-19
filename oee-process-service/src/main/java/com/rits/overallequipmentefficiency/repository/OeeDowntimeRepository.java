package com.rits.overallequipmentefficiency.repository;



import com.rits.overallequipmentefficiency.model.DowntimeModel;
import com.rits.overallequipmentefficiency.model.ResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@Repository
//public interface MachineLogRepository extends JpaRepository<MachineLogEntity,Long> {
//}
public interface OeeDowntimeRepository extends JpaRepository<DowntimeModel, Long> {



    @Query("SELECT d FROM DowntimeModel d WHERE d.site = :site AND d.resourceId = :resourceId AND d.active = :active")
    List<DowntimeModel> findActiveMachineDownBySite(@Param("site") String site, @Param("resourceId") String resourceId, @Param("active") Integer active);

    @Query("SELECT d FROM DowntimeModel d WHERE d.resourceId = :resourceId AND d.downtimeType IN :eventTypes AND d.active = :active")
    Optional<DowntimeModel> findDowntimeByEvent(
            @Param("resourceId") String resourceId,
            @Param("eventTypes") List<String> eventTypes,
            @Param("active") Integer active
    );

  //  List<Downtime> findActiveMachineDownBySite(String site, String resource, Integer active);

 //   void updateDowntime(Downtime downtime);

 //   void insertDowntime(Downtime downtime);

    @Modifying
    @Query("UPDATE DowntimeModel d SET d.active = 0 WHERE d.resourceId = :resourceId AND d.active = 1")
    void deactivateOldRecords(@Param("resourceId") String resourceId);

    @Query("SELECT d FROM DowntimeModel d WHERE d.site = :site AND d.resourceId = :resourceId " +
            "AND d.downtimeEnd > :startDateTime AND d.downtimeEnd <= :endDateTime " +
            "AND d.downtEvent = :downtEvent")
    List<DowntimeModel> findDowntimeWithinTimeRange(@Param("site") String site,
                                               @Param("resourceId") String resourceId,
                                               @Param("startDateTime") LocalDateTime startDateTime,
                                               @Param("endDateTime") LocalDateTime endDateTime,
                                               @Param("downtEvent") int downtEvent);

    @Query(value = "SELECT d.resource_id AS resourceId, " +
            "CASE WHEN COUNT(d.id) > 0 THEN true ELSE false END AS status " +
            "FROM r_downtime d " +
            "WHERE d.site = :site AND d.active = 1 AND d.downt_event = 0 " +
            "GROUP BY d.resource_id",
            nativeQuery = true)
    List<Map<String, Object>> findResourceStatusBySite(String site);

}
