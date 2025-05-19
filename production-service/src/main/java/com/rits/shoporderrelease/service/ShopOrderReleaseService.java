package com.rits.shoporderrelease.service;

import com.rits.shoporderrelease.dto.Bom;
import com.rits.shoporderrelease.dto.Item;
import com.rits.shoporderrelease.dto.ReleaseRequest;
import com.rits.shoporderrelease.model.SOReleaseMessageModel;
import com.rits.shoporderrelease.dto.ShopOrder;

import java.util.List;

public interface ShopOrderReleaseService {
    Boolean isItemAndBomAndRoutingReleasable(ShopOrder shopOrder,ReleaseRequest releaseRequest, Item existingItem)throws Exception;

    public SOReleaseMessageModel multiRelease(List<ReleaseRequest> releaseRequests) ;

    Boolean subOrderCreator(Bom bom, String shopOrder,ReleaseRequest releaseRequest,int totalPcuQuantityCreated) throws Exception;
}
