package com.rits.lineclearancelogservice.service;

import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequestList;
import com.rits.lineclearancelogservice.model.LineClearanceLog;
import com.rits.lineclearancelogservice.model.LineClearanceLogHistoryResponse;
import com.rits.lineclearancelogservice.model.MessageResponse;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;

import java.util.List;

public interface LineClearanceLogService {
    MessageResponse startLineClearanceLog(LineClearanceLogRequestList request) throws Exception;

    MessageResponse completeLineClearanceLog(LineClearanceLogRequestList request);

    MessageResponse rejectLineClearanceLog(LineClearanceLogRequest request);


    MessageResponse approveLineClearanceLog(LineClearanceLogRequest request);

    MessageResponse updateLineClearanceLog(LineClearanceLogRequest request);

    List<LineClearanceLogHistoryResponse> retriveLineClearanceLogHistory(LineClearanceLogRequest request);

    MessageResponse validateLineClearanceLog(LineClearanceLogRequest request);

    List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(LineClearanceLogRequest request);


    MessageResponse storeFile(LineClearanceLogRequestList  request);

    List<LineClearanceLog> retriveLineClearanceLogList(LineClearanceLogRequest request);

    boolean checkLineClearance(String site, String batchNo, String resourceId, String operation, String phase);

    boolean changeLineCleranceStatusToNew(String site, String batchNo, String resourceId);
}
