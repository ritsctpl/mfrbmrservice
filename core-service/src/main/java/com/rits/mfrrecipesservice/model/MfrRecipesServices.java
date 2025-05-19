package com.rits.mfrrecipesservice.model;

import com.rits.mfrrecipesservice.dto.*;

import java.util.List;

public interface MfrRecipesServices {
    public MfrMessageModel createMfr(RecordRequest request) throws Exception;

    public MfrRecipes retrieveMfrRecipes(MfrRecipesRequest mfrRecipesRequest) throws Exception;

    public MFRResponseList retrieveAllMfrRecipes(String site) throws Exception;

    public MFRResponse getNewMfr(String type, String site) throws Exception;

    public List<String> getMfrList(MfrRecipesRequest mfrRecipesRequest) throws Exception;

    public RMIResponseClass getSelectiveMfrData(MFRResponse mfrResponse) throws Exception;

    public MfrMessageModel getMfrToMfrConv(RecordRequest newMfrRequest) throws Exception;

}
