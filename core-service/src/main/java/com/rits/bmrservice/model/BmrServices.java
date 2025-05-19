package com.rits.bmrservice.model;

import com.rits.bmrservice.dto.*;
import com.rits.mfrrecipesservice.dto.MfrMessageModel;
import com.rits.mfrrecipesservice.dto.MfrRecipes;
import com.rits.mfrrecipesservice.dto.MfrRecipesRequest;
import com.rits.mfrrecipesservice.dto.RecordRequest;
import com.rits.mfrservice.dto.MFRResponseList;

import java.util.List;

public interface BmrServices {
    public BmrMessageModel createBmr(BmrRecipes bmrRequest) throws Exception;
//    public BmrMessageModel update(BmrRecipes bmrRequest) throws Exception;
    public BmrMessageModel retrieve(BmrRecipes bmrRequest) throws Exception;
    public BMRResponseList retrieveAll(BmrRecipes bmrRequest) throws Exception;
    public BmrMessageModel bmrPopulate(BmrRequest bmrRequest) throws Exception;
    public BMRResponse getNewBmr(String type, String site) throws Exception;
    public List<String> getBmrList(BmrRequest bmrRequest) throws Exception;

}
