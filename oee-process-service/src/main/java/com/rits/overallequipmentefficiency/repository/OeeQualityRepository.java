package com.rits.overallequipmentefficiency.repository;

import com.rits.overallequipmentefficiency.model.QualityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface OeeQualityRepository extends JpaRepository<QualityModel, Long> {
    /*@Query("SELECT q FROM QualityModel q " +
            "WHERE q.site = :site " +
            "AND ((:pcu IS NULL AND q.pcu IS NULL) OR q.pcu = :pcu) " +
            "AND q.shiftId = :shiftId " +
            "AND q.workcenterId = :workcenterId " +
            "AND q.resourceId = :resourceId " +
            "AND q.item = :item " +
            "AND q.itemVersion = :itemVersion " +
            "AND q.operation = :operation " +
            "AND q.operationVersion = :operationVersion " +
            "AND q.shopOrder = :shopOrder " +
            "AND q.batchNumber = :batchNumber " +
            "AND q.intervalStartDateTime >= :start " +
            "AND q.intervalEndDateTime <= :end")
    List<QualityModel> findByCriteriaAndInterval(
            @Param("site") String site,
            @Param("pcu") String pcu,
            @Param("shiftId") String shiftId,
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("shopOrder") String shopOrder,
            @Param("batchNumber") String batchNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
*/
    @Query("SELECT q FROM QualityModel q " +
            "WHERE ((:site IS NULL AND q.site IS NULL) OR q.site = :site) " +
            "AND ((:pcu IS NULL AND q.pcu IS NULL) OR q.pcu = :pcu) " +
            "AND ((:shiftId IS NULL AND q.shiftId IS NULL) OR q.shiftId = :shiftId) " +
            "AND ((:workcenterId IS NULL AND q.workcenterId IS NULL) OR q.workcenterId = :workcenterId) " +
            "AND ((:resourceId IS NULL AND q.resourceId IS NULL) OR q.resourceId = :resourceId) " +
            "AND ((:item IS NULL AND q.item IS NULL) OR q.item = :item) " +
            "AND ((:itemVersion IS NULL AND q.itemVersion IS NULL) OR q.itemVersion = :itemVersion) " +
            "AND ((:operation IS NULL AND q.operation IS NULL) OR q.operation = :operation) " +
            "AND ((:operationVersion IS NULL AND q.operationVersion IS NULL) OR q.operationVersion = :operationVersion) " +
            "AND ((:shopOrder IS NULL AND q.shopOrder IS NULL) OR q.shopOrder = :shopOrder) " +
            "AND ((:batchNumber IS NULL AND q.batchNumber IS NULL) OR q.batchNumber = :batchNumber) " +
            "AND q.intervalStartDateTime >= :start " +
            "AND q.intervalEndDateTime <= :end")
    List<QualityModel> findByCriteriaAndInterval(
            @Param("site") String site,
            @Param("pcu") String pcu,
            @Param("shiftId") String shiftId,
            @Param("workcenterId") String workcenterId,
            @Param("resourceId") String resourceId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("shopOrder") String shopOrder,
            @Param("batchNumber") String batchNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

}
