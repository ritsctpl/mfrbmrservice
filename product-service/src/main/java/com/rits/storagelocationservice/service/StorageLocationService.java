package com.rits.storagelocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.storagelocationservice.dto.*;
import com.rits.storagelocationservice.model.MessageModel;
import com.rits.storagelocationservice.model.StorageLocation;

public interface StorageLocationService {
   public StorageLocation createStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception;

    public StorageLocation updateStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception;

    public  StorageLocationResponseList getStorageLocationListByCreationDate(StorageLocationRequest storageLocationRequest) throws Exception;

    public StorageLocationResponseList getStorageLocationList(StorageLocationRequest storageLocationRequest) throws Exception;

    public StorageLocation retrieveStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception;

    public Response deleteStorageLocation(StorageLocationRequest storageLocationRequest) throws Exception;

    public Boolean isStorageLocationExist(StorageLocationRequest storageLocationRequest) throws Exception;

    public  StorageLocation associateWorkCenterToWorkCenterList(WorkCenterListRequest workCenterListRequest) throws Exception;

    public  StorageLocation removeWorkCenterToWorkCenterList(WorkCenterListRequest workCenterListRequest) throws Exception;

 AvailableWorkCenters getAvailableWorkCenters(StorageLocationRequest storageLocationRequest) throws Exception;
}
