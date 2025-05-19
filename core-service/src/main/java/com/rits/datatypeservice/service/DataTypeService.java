package com.rits.datatypeservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.datatypeservice.dto.DataFieldResponseList;
import com.rits.datatypeservice.dto.DataTypeRequest;
import com.rits.datatypeservice.dto.DataTypeResponseList;
import com.rits.datatypeservice.dto.Extension;
import com.rits.datatypeservice.model.DataType;
import com.rits.datatypeservice.model.MessageModel;

public interface DataTypeService {
    public MessageModel createDataType(DataTypeRequest dataTypeRequest) throws Exception;

    public MessageModel updateDataType(DataTypeRequest dataTypeRequest) throws Exception;

    public DataTypeResponseList getDataTypeListByCreationDate(DataTypeRequest dataTypeRequest) throws Exception;



    public DataTypeResponseList getDataTypeList(DataTypeRequest dataTypeRequest) throws Exception;

    DataTypeResponseList retrieveAllBySite(DataTypeRequest dataTypeRequest) throws Exception;

    public DataType retrieveDataType(DataTypeRequest dataTypeRequest) throws Exception;

    public MessageModel deleteDataType(DataTypeRequest dataTypeRequest) throws Exception;

    public Boolean isDataTypeExist(DataTypeRequest dataTypeRequest) throws Exception;

    public  DataFieldResponseList getDataFieldList(DataTypeRequest dataTypeRequest) throws Exception;

    String callExtension(Extension extension);
    public DataType retrieveDataType(String site, String bom , String revision, String component,String category) throws Exception;

}
