package com.rits.datacollectionservice.repository;

import com.rits.datacollectionservice.dto.DataCollectionResponse;
import com.rits.datacollectionservice.model.DataCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DataCollectionRepository extends MongoRepository<DataCollection, String> {
    boolean existsByActiveAndSiteAndDataCollectionAndVersion(int active, String site, String dataCollection, String version);

    DataCollection findByActiveAndSiteAndDataCollectionAndVersion(int active, String site, String dataCollection, String version);

    List<DataCollectionResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    List<DataCollectionResponse> findByActiveAndSiteAndDataCollectionContainingIgnoreCase(int active, String site, String dataCollection);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_Item(int active, String site, String item);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_Pcu(int active, String site, String pcu);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_ItemAndAttachmentList_Operation(int active, String site, String item, String operation);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_PcuAndAttachmentList_Operation(int active, String site, String pcu, String operation);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_ItemAndAttachmentList_OperationAndAttachmentList_Routing(int active, String site, String item, String operation, String routing);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_RoutingAndAttachmentList_Pcu(int active, String site, String operation, String routing, String pcu);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_Resource(int active, String site, String resource);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_ResourceAndAttachmentList_Pcu(int active, String site, String resource, String pcu);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_ShopOrderAndAttachmentList_Operation(int active, String site, String shopOrder, String operation);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_shopOrder(int active, String site, String shopOrder);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_workCenter(int active, String site, String workCenter);

    DataCollection findByActiveAndSiteAndDataCollectionAndCurrentVersion(int active, String site, String dataCollection, boolean currentVersion);

    List<DataCollection> findByActiveAndSiteAndDataCollection(int active, String site, String dataCollection);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_ResourceAndAttachmentList_Pcu(int active, String site, String operation, String resource, String pcu);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_Resource(int active, String site, String operation, String resource);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_OperationAndAttachmentList_Pcu(int active, String site, String operation, String pcu);

    List<DataCollection> findByActiveAndSiteAndAttachmentList_Operation(int i, String site, String operation);

    List<DataCollection> findByActiveAndSite(int i, String site);
}
