package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.PerformanceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OeePerformanceRepository extends JpaRepository<PerformanceModel, Long> {
    // Retrieves PerformanceModel records matching the criteria (including non-null fields) and whose createdDatetime is between :start and :end.
    @Query("SELECT p FROM PerformanceModel p " +
            "WHERE p.site = :site " +
            "AND ((:pcu IS NULL AND p.pcu IS NULL) OR p.pcu = :pcu) " +
            "AND ((:shiftId IS NULL AND p.shiftId IS NULL) OR p.shiftId = :shiftId) " +
            "AND ((:workcenterId IS NULL AND p.workcenterId IS NULL) OR p.workcenterId = :workcenterId) " +
            "AND ((:resourceId IS NULL AND p.resourceId IS NULL) OR p.resourceId = :resourceId) " +
            "AND ((:item IS NULL AND p.item IS NULL) OR p.item = :item) " +
            "AND ((:itemVersion IS NULL AND p.itemVersion IS NULL) OR p.itemVersion = :itemVersion) " +
            "AND ((:operation IS NULL AND p.operation IS NULL) OR p.operation = :operation) " +
            "AND ((:operationVersion IS NULL AND p.operationVersion IS NULL) OR p.operationVersion = :operationVersion) " +
            "AND ((:shopOrderBO IS NULL AND p.shopOrderBO IS NULL) OR p.shopOrderBO = :shopOrderBO) " +
            "AND ((:batchNumber IS NULL AND p.batchNumber IS NULL) OR p.batchNumber = :batchNumber) " +
            "AND p.intervalStartDateTime >= :start " +
            "AND p.intervalEndDateTime <= :end")
    List<PerformanceModel> findByCriteriaAndInterval(
            @Param("site") String site,
            @Param("pcu") String pcu,
            @Param("shiftId") String shiftId,
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("shopOrderBO") String shopOrderBO,
            @Param("batchNumber") String batchNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);




}