package com.rits.buyoffservice.repository;

import java.util.List;

import com.rits.buyoffservice.dto.BuyOffTop50Record;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.rits.buyoffservice.model.BuyOff;

public interface BuyOffRepository extends MongoRepository<BuyOff, String> {
    BuyOff findByHandle(String resource);

    List<BuyOffTop50Record> findTop50ByActiveAndSiteOrderByCreatedDateTimeAsc(int active, String site);
    List<BuyOffTop50Record> findByActiveAndSiteOrderByCreatedDateTimeAsc(int active, String site);

    List<BuyOff> findByActiveAndSiteAndAttachmentListResource(int active, String site, String resource);

    List<BuyOff> findByActiveAndSiteAndAttachmentListOperation(int active, String site, String operation);

    boolean existsByBuyOffAndSiteAndActiveEquals(String buyOff, String site, int active);

    BuyOff findByBuyOffAndVersionAndSiteAndActive(String buyOff,String version, String site, int active);

    List<BuyOff> findByActiveAndSiteAndAttachmentListWorkCenter(int active, String site, String workCenter);

    List<BuyOff> findByActiveAndSiteAndAttachmentListShopOrder(int active, String site, String shopOrder);

    List<BuyOff> findByActiveAndSiteAndAttachmentListPcu(int active, String site, String pcu);

    List<BuyOff> findByActiveAndSiteAndAttachmentListItem(int active, String site, String item);

    List<BuyOff> findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperation(int active, String site, String item, String operation);

    List<BuyOff> findByActiveAndSiteAndAttachmentListPcuAndAttachmentListOperation(int active, String site, String pcu, String operation);

    List<BuyOff> findByActiveAndSiteAndAttachmentListItemAndAttachmentListOperationAndAttachmentListRouting(int active, String site, String item, String operation, String routing);

    List<BuyOff> findByActiveAndSiteAndAttachmentListResourceAndAttachmentListPcu(int active, String site, String resource, String pcu);

    List<BuyOff> findByActiveAndSiteAndAttachmentListShopOrderAndAttachmentListOperation(int active, String site, String shopOrder, String operation);

    List<BuyOff> findByAttachmentListOperationAndAttachmentListRoutingAndAttachmentListPcu(String operation, String routing, String pcu);

    List<BuyOff> findByActiveAndSiteAndBuyOff(int i, String site, String buyOff);


    boolean existsByBuyOffAndVersionAndSiteAndActiveEquals(String buyOff, String version, String site, int i);

    boolean existsByHandleAndActive(String handle, int i);

    BuyOff findByActiveAndHandle(int i, String handle);
}
