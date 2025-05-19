package com.rits.toolnumberservice.service;

import com.rits.toolnumberservice.dto.MeasurementPointsResponseList;
import com.rits.toolnumberservice.dto.Response;
import com.rits.toolnumberservice.dto.ToolNumberRequest;
import com.rits.toolnumberservice.dto.ToolNumberResponseList;
import com.rits.toolnumberservice.model.ToolNumber;
import com.rits.toolnumberservice.model.ToolNumberMessageModel;

import java.util.List;

public interface ToolNumberService {
    public ToolNumberMessageModel createToolNumber(ToolNumberRequest toolNumberRequest) throws Exception;

    public ToolNumberMessageModel updateToolNumber(ToolNumberRequest toolNumberRequest) throws Exception;

    Boolean updateCurrentCount(ToolNumberRequest toolNumberRequest);

    public ToolNumberResponseList getToolNumberListByCreationDate(ToolNumberRequest toolNumberRequest) throws Exception;

    ToolNumberResponseList getEnabledToolNumber(ToolNumberRequest toolNumberRequest) throws Exception;

    public ToolNumberResponseList getToolNumberList(ToolNumberRequest toolNumberRequest) throws Exception;

    public ToolNumber retrieveToolNumber(ToolNumberRequest toolNumberRequest) throws Exception;

    List<ToolNumber> retrieveAllByToolGroup(String site, String toolGroup)throws Exception;

    public ToolNumberMessageModel deleteToolNumber(ToolNumberRequest toolNumberRequest) throws Exception;

    public Boolean isToolNumberExist(ToolNumberRequest toolNumberRequest) throws Exception;


    public MeasurementPointsResponseList getMeasurementPointsList(ToolNumberRequest toolNumberRequest) throws Exception;
}
