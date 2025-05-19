package com.rits.shoporderservice.repository;

import com.rits.shoporderservice.dto.ShopOrderList;
import com.rits.shoporderservice.dto.ShopOrderResponse;
import com.rits.shoporderservice.model.ShopOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShopOrderRepository extends MongoRepository<ShopOrder, String> {
    boolean existsBySiteAndActiveAndShopOrder(String site, int active, String shopOrder);

    ShopOrder findBySiteAndActiveAndShopOrder(String site, int active, String shopOrder);

    List<ShopOrderResponse> findByActiveAndSiteAndShopOrderContainingIgnoreCase(int active, String site, String shopOrder);

    List<ShopOrderResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<ShopOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedStartAfter(int active, String site, String status, LocalDateTime plannedStart);

    List<ShopOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedMaterial(int active, String site, String status, String item);

    List<ShopOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedWorkCenter(int active, String site, String status, String workCenter);

    List<ShopOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedRouting(int active, String site, String status, String router);

    List<ShopOrder> findByActiveAndSiteAndInUse(int active, String site, boolean inUse);
    List<ShopOrder> findByActiveAndSiteAndPlannedMaterialAndMaterialVersion(int active, String site, String plannedMaterial, String materialVersion);

    List<ShopOrderList> findByActiveAndSiteAndStatusIgnoreCaseAndPlannedCompletionAfter(int active, String site, String status, LocalDateTime plannedCompletion);

    ShopOrder findByActiveAndSiteAndSerialPcu_PcuNumber(int active, String site, String pcuNumber);
}
