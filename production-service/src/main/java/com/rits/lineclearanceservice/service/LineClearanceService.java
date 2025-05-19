package com.rits.lineclearanceservice.service;

import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.model.LineClearanceResponse;
import com.rits.lineclearanceservice.model.MessageModel;
import com.rits.lineclearanceservice.model.LineClearance;
import com.rits.lineclearanceservice.dto.LineClearanceRequest;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;

import java.util.List;

public interface LineClearanceService {

    MessageModel create(LineClearanceRequest request);

    MessageModel update(LineClearanceRequest request);

    MessageModel delete(LineClearanceRequest request);

    LineClearance retrieve(LineClearanceRequest request);

    List<LineClearanceResponse> retrieveAll(String site);
    List<LineClearanceResponse> retrieveTop50(String site);
    boolean isLineClearanceExist(String site, String templateName);
    public List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(RetrieveLineClearanceLogRequest request);
    boolean checkPermission(String templateName, String site, Integer active, String userId, String status);

}
