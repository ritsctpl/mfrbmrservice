package com.rits.productionlogservice.repository;

import com.rits.productionlogservice.dto.ProductionLogDTO;
import com.rits.productionlogservice.dto.ProductionLogDto;
import com.rits.productionlogservice.model.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionLogRepository extends JpaRepository<ProductionLog,Integer> {
    @Query("SELECT new com.rits.productionlogservice.dto.ProductionLogDto(p.site, p.workcenter_id, p.operation, p.operation_version, p.resource_id, p.item, p.item_version, p.shift_id, p.pcu) FROM ProductionLog p " +
            "WHERE p.created_datetime >= :startTime and p.site = :site and p.active = 1 " +
            "GROUP BY site, workcenter_id, operation, operation_version, resource_id, item, item_version, shift_id, pcu")
    List<ProductionLogDto> findUniqueCombinations(LocalDateTime startTime, String site);


    @Query("SELECT SUM(quantity_completed) FROM ProductionLog WHERE event_type = 'complete' " +
            "AND site = :site AND workcenter_id = :workcenter AND operation = :operation AND operation_version = :operationVersion " +
            "AND resource_id = :resource AND item = :item AND item_version = :itemVersion " +
            "AND shift_id = :shift AND pcu = :pcu")
    Double calculateTotalQuantity(String site, String workcenter, String operation, String operationVersion, String resource, String item, String itemVersion, String shift, String pcu);

    @Query("SELECT SUM(CASE WHEN event_type NOT IN ('scrap', 'rework') THEN quantity_completed ELSE 0 END) " +
            "FROM ProductionLog WHERE event_type = 'complete' AND site = :site " +
            "AND workcenter_id = :workcenter AND operation = :operation AND operation_version = :operationVersion AND resource_id = :resource " +
            "AND item = :item AND item_version = :itemVersion AND shift_id = :shift AND pcu = :pcu")
    Double calculateGoodQuantity(String site, String workcenter, String operation, String operationVersion, String resource, String item, String itemVersion, String shift, String pcu);

    @Query(nativeQuery = true, value =
            "SELECT SUM(p.quantity_completed) " +
                    "FROM r_production_log p " +
                    "WHERE p.event_type = 'complete' " +
                    "  AND (:site IS NULL OR p.site = :site) " +
                    "  AND (:workcenterId IS NULL OR p.workcenter_id = :workcenterId) " +
                    "  AND (:operation IS NULL OR p.operation = :operation) " +
                    "  AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
                    "  AND (:resourceId IS NULL OR p.resource_id = :resourceId) " +
                    "  AND (:item IS NULL OR p.item = :item) " +
                    "  AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
                    "  AND (:shiftId IS NULL OR p.shift_id = :shiftId) " +
                    "  AND (:pcu IS NULL OR p.pcu = :pcu)")
    Double calculateActualQuantity(String site, String workcenterId, String operation, String operationVersion, String resourceId, String item, String itemVersion, String shiftId, String pcu);

    @Query(nativeQuery = true, value =
            "WITH start_events AS ( " +
                    "    SELECT site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version, " +
                    "           MIN(created_datetime) AS first_start_time, MAX(actual_cycle_time) AS highest_start_cycle_time " +
                    "    FROM r_production_log " +
                    "    WHERE event_type = 'start' " +
                    "    GROUP BY site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version " +
                    "), " +
                    "complete_events AS ( " +
                    "    SELECT site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version, " +
                    "           MAX(created_datetime) AS last_complete_time, MAX(actual_cycle_time) AS highest_complete_cycle_time " +
                    "    FROM r_production_log " +
                    "    WHERE event_type = 'complete' " +
                    "    GROUP BY site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version " +
                    ") " +
                    "SELECT GREATEST(se.highest_start_cycle_time, ce.highest_complete_cycle_time) AS max_actual_cycle_time " +
                    "FROM start_events se " +
                    "JOIN complete_events ce " +
                    "    ON se.site = ce.site " +
                    "    AND se.shift_id = ce.shift_id " +
                    "    AND se.workcenter_id = ce.workcenter_id " +
                    "    AND se.resource_id = ce.resource_id " +
                    "    AND se.pcu = ce.pcu " +
                    "    AND se.operation = ce.operation " +
                    "    AND se.operation_version = ce.operation_version " +
                    "    AND se.item = ce.item " +
                    "    AND se.item_version = ce.item_version " +
                    "WHERE (:site IS NULL OR se.site = :site) " +
                    "AND (:shiftId IS NULL OR se.shift_id = :shiftId) " +
                    "AND (:workcenterId IS NULL OR se.workcenter_id = :workcenterId) " +
                    "AND (:resourceId IS NULL OR se.resource_id = :resourceId) " +
                    "AND (:pcu IS NULL OR se.pcu = :pcu) " +
                    "AND (:operation IS NULL OR se.operation = :operation) " +
                    "AND (:operationVersion IS NULL OR se.operation_version = :operationVersion) " +
                    "AND (:item IS NULL OR se.item = :item) " +
                    "AND (:itemVersion IS NULL OR se.item_version = :itemVersion)"
    )
    Double calculateActualCycleTime(String site, String shiftId, String workcenterId, String resourceId, String pcu, String operation, String operationVersion, String item, String itemVersion);


    @Query(nativeQuery = true, value =
            "WITH start_events AS ( " +
                    "    SELECT site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version, " +
                    "           MIN(created_datetime) AS first_start_time " +
                    "    FROM r_production_log " +
                    "    WHERE event_type = 'start' " +
                    "    GROUP BY site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version " +
                    "), " +
                    "done_events AS ( " +
                    "    SELECT site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version, " +
                    "           MAX(created_datetime) AS last_done_time " +
                    "    FROM r_production_log " +
                    "    WHERE event_type = 'done' " +
                    "    GROUP BY site, shift_id, workcenter_id, resource_id, pcu, operation, operation_version, item, item_version " +
                    ") " +
                    "SELECT " +
                    "       EXTRACT(EPOCH FROM (de.last_done_time - se.first_start_time)) AS manufactured_time_seconds " +
                    "FROM start_events se " +
                    "JOIN done_events de " +
                    "    ON se.site = de.site " +
                    "    AND se.shift_id = de.shift_id " +
                    "    AND se.workcenter_id = de.workcenter_id " +
                    "    AND se.resource_id = de.resource_id " +
                    "    AND se.pcu = de.pcu " +
                    "    AND se.operation = de.operation " +
                    "    AND se.operation_version = de.operation_version " +
                    "    AND se.item = de.item " +
                    "    AND se.item_version = de.item_version " +
                    "WHERE " +
                    "    (?1 IS NULL OR se.site = ?1) " +
                    "    AND (?2 IS NULL OR se.shift_id = ?2) " +
                    "    AND (?3 IS NULL OR se.workcenter_id = ?3) " +
                    "    AND (?4 IS NULL OR se.resource_id = ?4) " +
                    "    AND (?5 IS NULL OR se.pcu = ?5) " +
                    "    AND (?6 IS NULL OR se.operation = ?6) " +
                    "    AND (?7 IS NULL OR se.operation_version = ?7) " +
                    "    AND (?8 IS NULL OR se.item = ?8) " +
                    "    AND (?9 IS NULL OR se.item_version = ?9)")
    Double calculateManufacturedTime(
            String site,
            String shiftId,
            String workcenterId,
            String resourceId,
            String pcu,
            String operation,
            String operationVersion,
            String item,
            String itemVersion);

    @Query("SELECT p FROM ProductionLog p WHERE " +
            "(:eventType IS NULL OR p.event_type = :eventType) AND " +
            "(CAST(:dateTime AS timestamp) IS NULL OR p.created_datetime >= CAST(:dateTime AS timestamp))")
    List<ProductionLog> findByEventTypeAndDateTime(@Param("eventType") String eventType, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT p FROM ProductionLog p WHERE " +
            "(:pcu IS NULL OR p.pcu = :pcu) AND " +
            "(:shopOrderBO IS NULL OR p.shop_order_bo = :shopOrderBO) AND " +
            "(:item IS NULL OR p.item = :item) AND " +
            "(:itemVersion IS NULL OR p.item_version = :itemVersion)")
    List<ProductionLog> findByPcuShopOrderItemAndVersion(
            @Param("pcu") String pcu,
            @Param("shopOrderBO") String shopOrderBO,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion);

    @Query("SELECT p FROM ProductionLog p WHERE " +
            "(:batchNo IS NULL OR p.batchNo = :batchNo) AND " +
            "(:orderNumber IS NULL OR p.orderNumber = :orderNumber) AND " +
            "(:phaseId IS NULL OR p.phaseId = :phaseId) AND " +
            "(:operation IS NULL OR p.operation = :operation) AND " +
            "(:operationVersion IS NULL OR p.operation_version = :operationVersion) AND " +
            "(:item IS NULL OR p.item = :item) AND " +
            "(:itemVersion IS NULL OR p.item_version = :itemVersion)")
    List<ProductionLog> findByBatchNoAndOrderNoAndPhaseAndOperationAndOperationVersion(
            @Param("batchNo") String pcu,
            @Param("orderNumber") String orderNumber,
            @Param("phaseId") String phaseId,
            @Param("operation") String operation,
            @Param("operationVersion") String operation_version,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion);

    @Query(value = "SELECT * FROM R_PRODUCTION_LOG p WHERE " +
            "(:pcu IS NULL OR p.pcu = :pcu) AND " +
            "(:operation IS NULL OR p.operation = :operation) AND " +
            "(:operationVersion IS NULL OR p.operation_version = :operationVersion) AND " +
            "(:shopOrderBO IS NULL OR p.shop_order_bo = :shopOrderBO) AND " +
            "(:eventType IS NULL OR p.event_type = :eventType) AND " +  // <-- Added AND here
            "(:site IS NULL OR p.site = :site) AND " +
            "(:active IS NULL OR p.active = :active) " +
            "ORDER BY p.created_datetime ASC LIMIT 1",
            nativeQuery = true)
    ProductionLog findTopByPcuAndOperationAndShopOrderAndSiteAndActiveAndEventType(@Param("pcu") String pcu, @Param("operation") String operation, @Param("operationVersion") String operationVersion,
            @Param("shopOrderBO") String shopOrderBO, @Param("eventType") String eventType,@Param("site") String site, @Param("active") Integer active);

    @Query(value = "SELECT * FROM R_PRODUCTION_LOG p WHERE " +
            "(:pcu IS NULL OR p.pcu = :pcu) AND " +
            "(:shopOrderBO IS NULL OR p.shop_order_bo = :shopOrderBO) AND " +
            "(:eventType IS NULL OR p.event_type = :eventType) AND " +
            "(:site IS NULL OR p.site = :site) AND " +
            "(:active IS NULL OR p.active = :active) " +
            "ORDER BY p.created_datetime ASC LIMIT 1",
            nativeQuery = true)
    ProductionLog findFirstByPcuShopOrderSiteActiveAndEventType(
            @Param("pcu") String pcu,
            @Param("shopOrderBO") String shopOrderBO,
            @Param("eventType") String eventType,
            @Param("site") String site,
            @Param("active") Integer active
    );

    @Query("SELECT SUM(p.actual_cycle_time) " +
            "FROM ProductionLog p " +
            "WHERE p.pcu = :pcu " +
            "AND p.shop_order_bo = :shopOrderBO " +
            "AND p.operation = :operation " +
            "AND p.operation_version = :operationVersion " +
            "AND p.event_type = :eventType")
    Long getTotalActualCycleTime(@Param("pcu") String pcu, @Param("shopOrderBO") String shopOrderBO, @Param("operation") String operation, @Param("operationVersion") String operationVersion, @Param("eventType") String eventType);

    @Query("SELECT p FROM ProductionLog p WHERE p.site = :site AND p.pcu = :pcu AND p.event_type = :eventType")
    List<ProductionLog> findBySiteAndPcuAndEventType(@Param("site") String site, @Param("pcu") String pcu, @Param("eventType") String eventType);

    @Query("SELECT p FROM ProductionLog p WHERE p.site = :site AND p.resource_id = :resourceId AND p.event_type = :eventType ORDER BY p.created_datetime DESC")
    ProductionLog findTop1BySiteAndResourceIdAndEventTypeOrderByTimestampDesc(@Param("site") String site, @Param("resourceId") String resourceId, @Param("eventType") String eventType);

    @Query("SELECT pl.shift_id AS shiftId, pl.shop_order_bo AS shopOrder, SUM(pl.qty) AS totalQty, pl.event_type AS eventType , pl.batchNo AS batchNumber, " +
            "       pl.operation AS operation, " +
            "       pl.operation_version AS operationVersion, " +
            "       pl.item AS item, " +
            "       pl.item_version AS itemVersion, " +
            "       pl.reason_code AS reasonCode " +
            "FROM ProductionLog pl " +
            "WHERE (:site IS NULL OR pl.site = :site) " +
            "AND (:resourceId IS NULL OR pl.resource_id = :resourceId) " +
            "AND (:eventType IS NULL OR pl.event_type = :eventType) " +
            "AND pl.created_datetime BETWEEN CAST(:intervalStartDateTime AS timestamp) AND CAST(:intervalEndDateTime AS timestamp) " +
            "AND (:itemId IS NULL OR TRIM(LEADING '0' FROM pl.item) = TRIM(LEADING '0' FROM :itemId)) " +
            "AND (:itemVersion IS NULL OR pl.item_version = :itemVersion) " +
            "AND (:workcenterId IS NULL OR pl.workcenter_id = :workcenterId) " +
            "AND (:operationId IS NULL OR pl.operation = :operationId) " +
            "AND (:operationVersion IS NULL OR pl.operation_version = :operationVersion) " +
            "AND (:shiftId IS NULL OR pl.shift_id = :shiftId) " +
            "AND (:shopOrderBo IS NULL OR pl.shop_order_bo = :shopOrderBo) " +
            "AND (:batchNo IS NULL OR pl.batchNo = :batchNo) " +
            "GROUP BY pl.shift_id, pl.shop_order_bo, pl.event_type, pl.batchNo, pl.operation, pl.operation_version, pl.item, pl.item_version, pl.reason_code")
    List<Object[]> getTotalProducedQuantity(
            @Param("site") String site,
            @Param("resourceId") String resourceId,
            @Param("eventType") String eventType,
            @Param("intervalStartDateTime") LocalDateTime intervalStartDateTime,
            @Param("intervalEndDateTime") LocalDateTime intervalEndDateTime,
            @Param("itemId") String itemId,
            @Param("itemVersion") String itemVersion,
            @Param("workcenterId") String workcenterId,
            @Param("operationId") String operationId,
            @Param("operationVersion") String operationVersion,
            @Param("shiftId") String shiftId,
            @Param("shopOrderBo") String shopOrderBo ,
            @Param("batchNo") String batchNo
    );


    @Query("SELECT p FROM ProductionLog p WHERE p.created_datetime BETWEEN :startDatetime AND :endDatetime " +
            "AND p.site = :site AND p.active = :active")
    List<ProductionLog> findByCreatedDatetimeBetweenAndSiteAndActive(
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String site,
            Integer active);


  /*    @Query("SELECT p.resource_id, p.site, p.workcenter_id, p.shift_id, p.item, p.item_version, p.operation, p.operation_version, SUM(p.qty) AS totalProducedQty " +
            "FROM ProductionLog p " +
            "WHERE p.qty IS NOT NULL " +
            "AND p.created_datetime BETWEEN :startDatetime AND :endDatetime " +
            "AND (:resourceId IS NULL OR p.resource_id = :resourceId) " + // Use p.resource_id here
            "AND (:site IS NULL OR p.site = :site) " +
            "AND (:workcenter_id IS NULL OR p.workcenter_id = :workcenter_id) " +
            "AND (:shiftId IS NULL OR p.shift_id = :shiftId) " +
            "AND (:item IS NULL OR p.item = :item) " +
            "AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
            "AND (:operation IS NULL OR p.operation = :operation) " +
            "AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
            "GROUP BY p.resource_id, p.site, p.workcenter_id, p.shift_id, p.item, p.item_version, p.operation, p.operation_version " + // Use p.resource_id here
            "ORDER BY p.resource_id, p.site, p.workcenter_id, p.shift_id, p.item, p.item_version, p.operation, p.operation_version") // Use p.resource_id here
    List<ProductionLog> findTotalProducedQty(
            @Param("startDatetime") LocalDateTime startDatetime,
            @Param("endDatetime") LocalDateTime endDatetime,
            @Param("resourceId") String resourceId,
            @Param("site") String site,
            @Param("workcenterId") String workcenterId,
            @Param("shiftId") String shiftId,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion);*/

    @Query(value = "WITH production_log_filtered AS (" +
            "    SELECT " +
            "        resource_id AS resourceId, " +
            "        workcenter_id AS workcenterId, " +
            "        site, " +
            "        shift_id AS shiftId, " +
            "        item, " +
            "        item_version AS itemVersion, " +
            "        batch_no AS batchNo, " +
            "        shop_order_bo AS shopOrderBo, " +
            "        event_type AS eventType, " +
            "        event_datetime AS eventDateTime " +
            "    FROM r_production_log " +
            "    WHERE event_datetime BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59' " +
            "    AND site = :site " +
            "    AND (:resourceId IS NULL OR resource_id = :resourceId) " +
            "    AND (:workcenterId IS NULL OR workcenter_id = :workcenterId) " +
            "    AND (:operation IS NULL OR operation = :operation) " +
            "    AND (:shiftId IS NULL OR shift_id = :shiftId) " +
            "    AND (:shopOrderBo IS NULL OR shop_order_bo = :shopOrderBo) " +
            "    AND (:batchNo IS NULL OR batch_no = :batchNo) " +
            "), availability_filtered AS (" +
            "    SELECT " +
            "        resource_id AS resourceId, " +
            "        workcenter_id AS workcenterId, " +
            "        site, " +
            "        shift_id AS shiftId, " +
            "        SUM(actualavailabletime * 60) AS totalAvailableTimeSeconds " +
            "    FROM r_availability " +
            "    WHERE availability_date BETWEEN '2024-01-01' AND '2024-12-31' " +
            "    AND site = :site " +
            "    GROUP BY resource_id, workcenter_id, site, shift_id" +
            "), cycle_time_filtered AS (" +
            "    SELECT " +
            "        resource_id AS resourceId, " +
            "        workcenter_id AS workcenterId, " +
            "        site, " +
            "        shift_id AS shiftId, " +
            "        item, " +
            "        item_version AS itemVersion, " +
            "        operation, " +
            "        operation_version AS operationVersion, " +
            "        MAX(planned_cycle_time) AS cycleTimeSeconds " +
            "    FROM r_cycle_time " +
            "    WHERE site = :site " +
            "    GROUP BY resource_id, workcenter_id, site, shift_id, item, item_version, operation, operation_version" +
            "), parts_to_be_produced AS (" +
            "    SELECT " +
            "        pl.resource_id, " +
            "        pl.workcenter_id, " +
            "        pl.site, " +
            "        pl.shift_id, " +
            "        pl.item, " +
            "        pl.item_version, " +
            "        pl.batch_no, " +
            "        pl.shop_order_bo, " +
            "        af.total_available_time_seconds AS totalAvailableTimeSeconds, " +
            "        cf.cycle_time_seconds AS cycleTimeSeconds, " +
            "        (af.total_available_time_seconds * 60) / cf.cycle_time_seconds AS partsToBeProduced " +
            "    FROM production_log_filtered pl " +
            "    JOIN availability_filtered af " +
            "        ON pl.resourceId = af.resourceId " +
            "        AND pl.workcenter_id = af.workcenterId " +
            "        AND pl.site = af.site " +
            "        AND pl.shift_id = af.shiftId " +
            "    JOIN cycle_time_filtered cf " +
            "        ON pl.resourceId = cf.resourceId " +
            "        AND pl.workcenter_id = cf.workcenterId " +
            "        AND pl.site = cf.site " +
            "        AND pl.shift_id = cf.shiftId " +
            "        AND pl.item = cf.item " +
            "        AND pl.item_version = cf.itemVersion " +
            ") " +
            "SELECT " +
            "    resourceId AS resource_id, " +
            "    workcenterId AS workcenter_id, " +
            "    site, " +
            "    shiftId AS shift_id, " +
            "    item, " +
            "    itemVersion AS item_version, " +
            "    batchNo AS batch_no, " +
            "    shopOrderBo AS shop_order_bo, " +
            "    partsToBeProduced AS parts_to_be_produced " +
            "FROM parts_to_be_produced", nativeQuery = true)
    List<ProductionLogDTO> getPartsToBeProduced(@Param("site") String site,
                                                @Param("resourceId") String resourceId,
                                                @Param("workcenterId") String workcenterId,
                                                @Param("operation") String operation,
                                                @Param("shiftId") String shiftId,
                                                @Param("shopOrderBo") String shopOrderBo,
                                                @Param("batchNo") String batchNo);

    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_datetime BETWEEN :startDate AND :endDate " +
            "AND p.site = :site " +
            "AND p.event_type = :eventType")
    List<ProductionLog> findProductionLogs(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("site") String site,
                                           @Param("eventType") String eventType);

    @Query("SELECT p FROM ProductionLog p WHERE " +
            "(:site IS NULL OR COALESCE(:site, '') = '' OR p.site = :site) AND " +
            "(:batchNo IS NULL OR COALESCE(:batchNo, '') = '' OR " +
            " p.batchNo = :batchNo OR p.batchNo LIKE CONCAT('%_', :batchNo)) AND " +
            "(:orderNumber IS NULL OR COALESCE(:orderNumber, '') = '' OR p.orderNumber = :orderNumber) AND " +
            "(:material IS NULL OR COALESCE(:material, '') = '' OR p.material = :material) AND " +
            "(:materialVersion IS NULL OR COALESCE(:materialVersion, '') = '' OR p.materialVersion = :materialVersion) AND " +
            "(:phaseId IS NULL OR COALESCE(:phaseId, '') = '' OR p.phaseId = :phaseId) AND " +
            "(:operation IS NULL OR COALESCE(:operation, '') = '' OR p.operation = :operation) AND " +
//            "(:operationVersion IS NULL OR COALESCE(:operationVersion, '') = '' OR p.operation_version = :operationVersion) AND " +
            "(:user IS NULL OR COALESCE(:user, '') = '' OR p.user_id = :user) AND " +
            "(p.created_datetime >= :startDate AND p.created_datetime <= :endDate) AND " +
            "p.active = 1 ORDER BY p.created_datetime DESC")

    List<ProductionLog> findByCustomCriteria(
            @Param("site") String site,
            @Param("batchNo") String batchNo,
            @Param("orderNumber") String orderNumber,
            @Param("material") String material,
            @Param("materialVersion") String materialVersion,
            @Param("phaseId") String phaseId,
            @Param("operation") String operation,
//            @Param("operationVersion") String operationVersion,
            @Param("user") String user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM ProductionLog p WHERE " +
            "(:site IS NULL OR COALESCE(:site, '') = '' OR p.site = :site) AND " +
            "(:user IS NULL OR COALESCE(:user, '') = '' OR p.user_id = :user) AND " +
            "(COALESCE(:pcu, '') <> '' AND p.pcu = :pcu OR :pcu IS NULL) AND " +
            "(COALESCE(:shopOrderBo, '') <> '' AND p.shop_order_bo = :shopOrderBo OR :shopOrderBo IS NULL) AND " +
//            "((COALESCE(:material, '') <> '' AND p.material = :material) OR :material IS NULL) AND " +
//            "((COALESCE(:materialVersion, '') <> '' AND p.materialVersion = :materialVersion) OR :materialVersion IS NULL) AND " +
           "((COALESCE(:item, '') <> '' AND p.item = :item) OR :item IS NULL) AND " +
            "((COALESCE(:itemVersion, '') <> '' AND p.item_version = :itemVersion) OR :itemVersion IS NULL) AND " +
//            "(p.created_datetime BETWEEN :startTime AND :endTime) AND " +
            "(p.created_datetime >= :startTime AND p.created_datetime <= :endTime) AND " +
            "p.pcu IS NOT NULL AND " +
            "p.active = 1 " +
            "ORDER BY p.created_datetime DESC")
    List<ProductionLog> findByPCUCriteria(
            @Param("site") String site,
            @Param("user") String user,
            @Param("pcu") String pcu,
            @Param("shopOrderBo") String shopOrderBo,
//            @Param("material") String material,
//            @Param("materialVersion") String materialVersion,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
//AND p.item IS NOT NULL AND p.item <> ''
    @Query("SELECT DISTINCT p.item, p.item_version FROM ProductionLog p WHERE p.resource_id = :resourceId AND p.event_datetime BETWEEN :startDateTime AND :endDateTime ")
    List<Object[]> getUniqueItemVersions(@Param("resourceId") String resourceId,
                                         @Param("startDateTime") LocalDateTime startDateTime,
                                         @Param("endDateTime") LocalDateTime endDateTime);


    @Query("SELECT DISTINCT pl.item, pl.item_version " +
            "FROM ProductionLog pl " +
            "WHERE pl.resource_id IN :resources " +
            "AND pl.event_datetime BETWEEN :startDateTime AND :endDateTime")
    List<Object[]> getUniqueItemVersionsByResources(@Param("resources") List<String> resources,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);


    @Query("SELECT DISTINCT pl.item, pl.item_version " +
            "FROM ProductionLog pl " +
            "WHERE pl.resource_id IN :resources " +
            "AND pl.event_datetime BETWEEN :startDateTime AND :endDateTime " +
            "AND pl.event_type in ('doneSfcBatch','machineDoneSfcBatch') ")
    List<Object[]> getUniqueItemVersionsByResourcesEventTypeDone(@Param("resources") List<String> resources,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);




    // 1. First created_datetime by event_type and shop_order_bo (order-based)
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.shop_order_bo = :shopOrderBo " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC")
    Optional<ProductionLog> findFirstByEventTypeAndShopOrderBoAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
            @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 2. First created_datetime by event_type, batchNo, operation, and operation_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.operation = :operation " +
            "  AND p.operation_version = :operationVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC")
    Optional<ProductionLog> findFirstByEvent_typeAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
            @Param("eventType") String eventType,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 3. First created_datetime by event_type and batchNo
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.batchNo = :batchNo " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC")
    Optional<ProductionLog> findFirstByEvent_typeAndBatchNoAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
            @Param("eventType") String eventType,
            @Param("batchNo") String batchNo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 4. Last created_datetime by event_type and shop_order_bo (order-based)
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.shop_order_bo = :shopOrderBo " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC")
    Optional<ProductionLog> findFirstByEvent_typeAndShop_order_boAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
            @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 5. Last created_datetime by event_type, batchNo, operation, and operation_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.operation = :operation " +
            "  AND p.operation_version = :operationVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC")
    Optional<ProductionLog> findFirstByEvent_typeAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
            @Param("eventType") String eventType,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 6. Last created_datetime by event_type and batchNo
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.batchNo = :batchNo " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC")
    Optional<ProductionLog> findFirstByEvent_typeAndBatchNoAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
            @Param("eventType") String eventType,
            @Param("batchNo") String batchNo,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Existing methods (order-based and batch-based) would be here…

    // 7. First created_datetime by event_type, item, and item_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.item = :item " +
            "  AND p.item_version = :itemVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC")
    Optional<ProductionLog> findFirstByEvent_typeAndItemAndItem_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
            @Param("eventType") String eventType,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 8. Last created_datetime by event_type, item, and item_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.item = :item " +
            "  AND p.item_version = :itemVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC")
    Optional<ProductionLog> findFirstByEvent_typeAndItemAndItem_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
            @Param("eventType") String eventType,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // --- Existing methods for order and batch queries go here ---

    // 7. First created_datetime by event_type, item, item_version, operation, and operation_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.item = :item " +
            "  AND p.item_version = :itemVersion " +
            "  AND p.operation = :operation " +
            "  AND p.operation_version = :operationVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC")
    Optional<ProductionLog> findFirstByEvent_typeAndItemAndItem_versionAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeAsc(
            @Param("eventType") String eventType,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 8. Last created_datetime by event_type, item, item_version, operation, and operation_version
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.event_type = :eventType " +
            "  AND p.item = :item " +
            "  AND p.item_version = :itemVersion " +
            "  AND p.operation = :operation " +
            "  AND p.operation_version = :operationVersion " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC")
    Optional<ProductionLog> findFirstByEvent_typeAndItemAndItem_versionAndOperationAndOperation_versionAndCreated_datetimeBetweenOrderByCreated_datetimeDesc(
            @Param("eventType") String eventType,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Generic query: find the earliest created_datetime by all criteria.
    @Query(value = "SELECT p.created_datetime FROM R_PRODUCTION_LOG p " +
            "WHERE (:site IS NULL OR p.site = :site) " +
            "  AND (:resourceId IS NULL OR p.resource_id = :resourceId) " +
            //"  AND (:eventType IS NULL OR p.event_type = :eventType) " +
            "  AND (p.event_type IN ('startSfcBatch','doneSfcBatch','completeSfcBatch','Start','scrapSfcBatch')) " +
            "  AND (:shopOrderBo IS NULL OR p.shop_order_bo = :shopOrderBo) " +
            "  AND (:batchNo IS NULL OR p.batch_no = :batchNo) " +
            "  AND (:workcenterId IS NULL OR p.workcenter_id = :workcenterId) " +
            "  AND (:operation IS NULL OR p.operation = :operation) " +
            "  AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
            "  AND (:item IS NULL OR p.item = :item) " +
            "  AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC LIMIT 1",
            nativeQuery = true)
    Optional<LocalDateTime> findFirstCreatedDateTimeByCriteria(
            @Param("site") String site,
            @Param("resourceId") String resourceId,
          //  @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("batchNo") String batchNo,
            @Param("workcenterId") String workcenterId,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Generic query: find the latest created_datetime by all criteria.
    @Query(value = "SELECT p.created_datetime FROM R_PRODUCTION_LOG p " +
            "WHERE (:site IS NULL OR p.site = :site) " +
            "  AND (:resourceId IS NULL OR p.resource_id = :resourceId) " +
            "  AND p.event_type IN ('startSfcBatch','completSfcBatch','doneSfcBatch','ScrapSFC') " +
            "  AND (:shopOrderBo IS NULL OR p.shop_order_bo = :shopOrderBo) " +
            "  AND (:batchNo IS NULL OR p.batch_no = :batchNo) " +
            "  AND (:workcenterId IS NULL OR p.workcenter_id = :workcenterId) " +
            "  AND (:operation IS NULL OR p.operation = :operation) " +
            "  AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
            "  AND (:item IS NULL OR p.item = :item) " +
            "  AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC LIMIT 1",
            nativeQuery = true)
    Optional<LocalDateTime> findLastCreatedDateTimeByCriteria(
            @Param("site") String site,
            @Param("resourceId") String resourceId,
       //     @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("batchNo") String batchNo,
            @Param("workcenterId") String workcenterId,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Generic query: find the earliest created_datetime by all criteria.
    @Query(value = "SELECT p.created_datetime FROM R_PRODUCTION_LOG p " +
            "WHERE (:site IS NULL OR p.site = :site) " +
            "  AND (:resourceId IS NULL OR p.resource_id = :resourceId) " +
            //"  AND (:eventType IS NULL OR p.event_type = :eventType) " +
            "  AND (p.event_type IN ('machineStartSfcBatch','machineDoneSfcBatch','machineCompleteSfcBatch','machineScrapSfcBatch')) " +
            "  AND (:shopOrderBo IS NULL OR p.shop_order_bo = :shopOrderBo) " +
            "  AND (:batchNo IS NULL OR p.batch_no = :batchNo) " +
            "  AND (:workcenterId IS NULL OR p.workcenter_id = :workcenterId) " +
            "  AND (:operation IS NULL OR p.operation = :operation) " +
            "  AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
            "  AND (:item IS NULL OR p.item = :item) " +
            "  AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime ASC LIMIT 1",
            nativeQuery = true)
    Optional<LocalDateTime> findFirstCreatedDateTimeByCriteriabyMachine(
            @Param("site") String site,
            @Param("resourceId") String resourceId,
            //  @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("batchNo") String batchNo,
            @Param("workcenterId") String workcenterId,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Generic query: find the latest created_datetime by all criteria.
    @Query(value = "SELECT p.created_datetime FROM R_PRODUCTION_LOG p " +
            "WHERE (:site IS NULL OR p.site = :site) " +
            "  AND (:resourceId IS NULL OR p.resource_id = :resourceId) " +
            "  AND p.event_type IN ('machineStartSfcBatch','machineCompletSfcBatch','machineDoneSfcBatch','machineScrapSFC') " +
            "  AND (:shopOrderBo IS NULL OR p.shop_order_bo = :shopOrderBo) " +
            "  AND (:batchNo IS NULL OR p.batch_no = :batchNo) " +
            "  AND (:workcenterId IS NULL OR p.workcenter_id = :workcenterId) " +
            "  AND (:operation IS NULL OR p.operation = :operation) " +
            "  AND (:operationVersion IS NULL OR p.operation_version = :operationVersion) " +
            "  AND (:item IS NULL OR p.item = :item) " +
            "  AND (:itemVersion IS NULL OR p.item_version = :itemVersion) " +
            "  AND p.created_datetime BETWEEN :start AND :end " +
            "ORDER BY p.created_datetime DESC LIMIT 1",
            nativeQuery = true)
    Optional<LocalDateTime> findLastCreatedDateTimeByCriteriabyMachine(
            @Param("site") String site,
            @Param("resourceId") String resourceId,
            //     @Param("eventType") String eventType,
            @Param("shopOrderBo") String shopOrderBo,
            @Param("batchNo") String batchNo,
            @Param("workcenterId") String workcenterId,
            @Param("operation") String operation,
            @Param("operationVersion") String operationVersion,
            @Param("item") String item,
            @Param("itemVersion") String itemVersion,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query(value = "SELECT * FROM R_PRODUCTION_LOG " +
            "WHERE site = :site " +
            "AND order_number = :orderNumber " +
            "AND phase_id = :phaseId " +
            "AND operation = :operation " +
            "AND event_type = :eventType",
            nativeQuery = true)
    List<ProductionLog> findBySiteAndOrderNumberAndPhaseIdAndOperationAndEventType(
            @Param("site") String site,
            @Param("orderNumber") String orderNumber,
            @Param("phaseId") String phaseId,
            @Param("operation") String operation,
            @Param("eventType") String eventType
    );
    @Query("SELECT p FROM ProductionLog p " +
            "WHERE p.batchNo = :batchNo AND p.site = :site " +
            "AND (p.event_type = 'startSfcBatch' OR p.event_type = 'doneSfcBatch') " +
            "ORDER BY p.created_datetime ASC")
    List<ProductionLog> findBatchCycleTimes(@Param("batchNo") String batchNo, @Param("site") String site);
}




