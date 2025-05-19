package com.rits.processorderservice.repository;

import com.rits.processorderservice.dto.ProcessOrderList;
import com.rits.processorderservice.dto.ProcessOrderResponse;
import com.rits.processorderservice.model.ProcessOrder;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProcessOrderRepository extends MongoRepository<ProcessOrder, String> {
  //  boolean existsBySiteAndActiveAndProcessOrder(String site, int active, String processOrder);

//    ProcessOrder findBySiteAndActiveAndProcessOrder(String site, int active, String processOrder);
//
//    List<ProcessOrderResponse> findByActiveAndSiteAndProcessOrderContainingIgnoreCase(int active, String site, String processOrder);
//
//    List<ProcessOrderResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    @Query("{ 'active': ?1, 'site': ?0, 'availableQtyToRelease': { $gt: 0 } }")
    List<ProcessOrder> findByActiveAndSiteAndAvailableQtyToReleaseGreaterThan(String site, int active);
    List<ProcessOrder> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

//
//    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedStartAfter(int active, String site, String status, LocalDateTime plannedStart);
//
//    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedMaterial(int active, String site, String status, String item);
//
//    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedWorkCenter(int active, String site, String status, String workCenter);
//
//    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedRouting(int active, String site, String status, String router);

    List<ProcessOrder> findByActiveAndSiteAndInUse(int active, String site, boolean inUse);
//    List<ProcessOrder> findByActiveAndSiteAndPlannedMaterialAndMaterialVersion(int active, String site, String plannedMaterial, String materialVersion);
//
//    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedCompletionAfter(int active, String site, String status, LocalDateTime plannedCompletion);
//
//    ProcessOrder findByActiveAndSiteAndSerialBatchNumber_bno(int active, String site, String bno);
//
    boolean existsBySiteAndOrderNumberAndActive(String site, String orderNumber,int active);


    List<ProcessOrderResponse> findByActiveAndSiteAndOrderNumberContainingIgnoreCase( int i,String site, String orderNumber);

    ProcessOrder findBySiteAndActiveAndOrderNumber(String site, int i, String orderNumber);

    List<ProcessOrder> findBySiteAndActiveAndOrderNumberContainingIgnoreCase(String site, int i, String orderNumber);
    List<ProcessOrder> findTop50BySiteAndOrderNumberContainingIgnoreCase(String site, String orderNumber);
    List<ProcessOrder> findTop50BySiteAndOrderNumberContainingIgnoreCaseAndActive(String site, String orderNumber,int active);

    List<ProcessOrder> findTop50BySite(String site);
    List<ProcessOrder> findTop50BySiteAndActive(String site,int active);

    ProcessOrder findByActiveAndSiteAndBatchNumber_BatchNumber(int i, String site, String sfcNumber);

    Boolean existsBySiteAndActiveAndOrderNumber(String site, int i, String orderNumber);

    List<ProcessOrder> findByActiveAndSiteAndMaterialAndMaterialVersion(int i, String site, String material, String materialVersion);

    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndProductionStartDateAfter(int active, String site, String status, LocalDateTime productionStartDate);

    List<ProcessOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndProductionFinishDateAfter(int active, String site, String status, LocalDateTime productionFinishDate);

    ProcessOrderResponse findBySiteAndOrderNumberAndActive(String site, String orderNumber, int i);
}
