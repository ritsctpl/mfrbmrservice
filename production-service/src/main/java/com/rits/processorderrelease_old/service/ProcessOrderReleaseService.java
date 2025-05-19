package com.rits.processorderrelease_old.service;

import com.rits.processorderrelease_old.dto.Bom;
import com.rits.processorderrelease_old.dto.ReleaseRequest;
import com.rits.processorderrelease_old.model.POReleaseMessageModel;

import java.util.List;

public interface ProcessOrderReleaseService {
    //Boolean isItemAndBomAndRoutingReleasable(ShopOrder shopOrder, ReleaseRequest releaseRequest, Item existingItem)throws Exception;

    public POReleaseMessageModel multiRelease(List<ReleaseRequest> releaseRequests) ;

    Boolean subOrderCreator(Bom bom, String shopOrder, ReleaseRequest releaseRequest, int totalPcuQuantityCreated) throws Exception;
}
