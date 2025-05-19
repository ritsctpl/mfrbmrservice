package com.rits.dataFieldService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.dataFieldService.dto.DataFieldRequest;
import com.rits.dataFieldService.dto.DataFieldResponseList;
import com.rits.dataFieldService.dto.Extension;
import com.rits.dataFieldService.model.DataField;
import com.rits.dataFieldService.model.MessageModel;


public interface DataFieldService {
    public DataFieldResponseList getDataFieldListByCreationDate(String site) throws Exception;

    public DataFieldResponseList getDataFieldList(String dataField, String site) throws Exception;

    public DataField retrieveDataField(String dataField, String site) throws Exception;

    public MessageModel createDataField(DataFieldRequest dataFieldRequest) throws Exception;

    public MessageModel deleteDataField(String dataField, String site,String userId) throws Exception;

    public MessageModel updateDataField(DataFieldRequest dataFieldRequest) throws Exception;

    public String callExtension(Extension extension) throws Exception;
    public boolean isExist(String site,String dataField) throws Exception;
    public boolean isTrackable(String site,String dataField) throws Exception;

}
