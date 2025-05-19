package com.rits.cycletimeservice.repository;

import com.rits.cycletimeservice.model.CycleTimePostgres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CycleTimePostgresRepository extends JpaRepository<CycleTimePostgres, Long> {

    CycleTimePostgres findBySiteAndActiveAndHandle(String site, int i, String handle);

    List<CycleTimePostgres> findBySiteAndActiveOrderByCreatedDatetimeDesc(String site, int i);

    List<CycleTimePostgres> findByResourceIdAndOperationAndOperationVersionAndItemAndItemVersionAndSiteAndActive(
            String resourceId, String operation, String operationVersion, String item, String itemMaterial,
            String site, int active);

    Boolean existsBySiteAndActiveAndHandle(String site, int i, String handle);

    Boolean existsByTag(String tag);


    @Query(value = "SELECT c.planned_cycle_time " +
            "FROM r_cycle_time_maintenance c " +
            "WHERE (:site IS NULL OR c.site = :site) " +
            "AND (:workcenterId IS NULL OR c.workcenter_id = :workcenterId) " +
            "AND (:operation IS NULL OR c.operation = :operation) " +
            "AND (:operationVersion IS NULL OR c.operation_version = :operationVersion) " +
            "AND (:resourceId IS NULL OR c.resource_id = :resourceId) " +
            "AND (:item IS NULL OR c.item = :item) " +
            "AND (:itemVersion IS NULL OR c.item_version = :itemVersion) " +
            "AND (:shiftId IS NULL OR c.shift_id = :shiftId) " +
            "AND (:pcu IS NULL OR c.pcu = :pcu)", nativeQuery = true)
    Double findPlannedCycleTime(String site,String workcenterId, String operation, String operationVersion, String resourceId, String item, String itemVersion, String shiftId, String pcu);
}

