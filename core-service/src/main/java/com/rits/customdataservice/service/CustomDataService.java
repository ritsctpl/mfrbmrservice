package com.rits.customdataservice.service;

import java.util.List;


import com.fasterxml.jackson.databind.JsonNode;
import com.rits.customdataservice.dto.CategoryList;
import com.rits.customdataservice.dto.CustomDataRequest;
import com.rits.customdataservice.dto.Extension;
import com.rits.customdataservice.dto.Response;
import com.rits.customdataservice.model.CustomData;
import com.rits.customdataservice.model.CustomDataList;
import com.rits.customdataservice.model.MessageModel;

public interface CustomDataService {
    public MessageModel createCustomData(CustomDataRequest customData) throws Exception;

    public List<CustomDataList> retrieveCustomDataListByCategory(CustomDataRequest customDataRequest) throws Exception;

    public MessageModel updateCustomData(CustomDataRequest customDataRequest) throws Exception;

    public MessageModel deleteCustomData(CustomDataRequest customDataRequest) throws Exception;

    public CategoryList retrieveTop50(CustomDataRequest customDataRequest) throws Exception;

    public String callExtension(Extension extension);

}
