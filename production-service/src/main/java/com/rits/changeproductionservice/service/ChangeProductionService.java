package com.rits.changeproductionservice.service;

import com.rits.changeproductionservice.dto.ChangeProductionRequest;
import com.rits.pcurouterheaderservice.dto.Operation;

import java.util.List;

public interface ChangeProductionService {
    String getFirstOperation(String site, String routing, String routingVersion)throws Exception;

    String getCurrentOperation(String site, String routing, String routingVersion, String pcu) throws Exception;

    String getFirstUncompletedOperation(String site, String routing, String routingVersion, String pcu)throws Exception;

    String  doChangeProduction(ChangeProductionRequest changeProductionRequest) throws  Exception;
}
