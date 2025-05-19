package com.rits.uomservice.service;

import com.rits.uomservice.dto.UOMMessageModel;
import com.rits.uomservice.dto.UOMRequest;
import com.rits.uomservice.dto.UOMResponse;
import com.rits.uomservice.dto.UOMResponseList;
import com.rits.uomservice.model.UOMEntity;

import java.util.Optional;

public interface UOMService {
//    UOMResponse create(UOMRequest uomRequest) throws Exception;
//    Optional<UOMResponse> retrieve(Integer id) throws Exception;
//    UOMResponse update(Integer id, UOMRequest uomRequest) throws Exception;
//    void delete(Integer id) throws Exception;

    UOMMessageModel createUOM(UOMRequest uomRequest) throws Exception;

    UOMMessageModel updateUOM(UOMRequest uomRequest) throws Exception;


    UOMMessageModel deleteUOM(UOMRequest uomRequest) throws Exception;



    UOMEntity retrieveUOM(UOMRequest uomRequest) throws Exception;



    UOMResponseList retrieveAll(UOMRequest uomRequest) throws Exception;
    UOMMessageModel createBaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel updateBaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel deleteBaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel retrieveBaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel retrieveAllBaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel retrieveTop50BaseUnitConvertion(UOMRequest uomRequest) throws Exception;
    String unitConvertion(UOMRequest uomRequest) throws Exception;
    UOMMessageModel getUomByFilteredName(UOMRequest uomRequest) throws Exception;

    UOMResponseList retrieveTop50(String site) throws Exception;
}
