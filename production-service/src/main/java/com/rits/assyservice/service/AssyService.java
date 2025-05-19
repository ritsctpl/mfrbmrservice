package com.rits.assyservice.service;

import com.rits.assyservice.dto.AssyRequest;
import com.rits.assyservice.model.AssyData;
import com.rits.assyservice.model.MessageModel;

import java.util.List;

public interface AssyService {
    boolean assemble(AssyRequest request);

    AssyData saveAssyData(AssyRequest assyData);

    AssyData setAncestry(AssyData assyData);

    AssyData getTreeStructure(String site, String itemBO, String pcuBO) throws Exception;

    boolean remove(String site, String pcuBO, AssyData.Component component) throws Exception;

    AssyData retrieve(String site, String pcuBO);
}