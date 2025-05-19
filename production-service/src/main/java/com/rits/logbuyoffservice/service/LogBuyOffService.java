package com.rits.logbuyoffservice.service;

import com.rits.logbuyoffservice.dto.AttachmentDetailsRequest;
import com.rits.logbuyoffservice.dto.LogbuyOffRequest;
import com.rits.logbuyoffservice.dto.LogbuyOffRequestList;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.model.LogBuyOffMessageModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LogBuyOffService {
    LogBuyOffMessageModel accept(LogbuyOffRequestList logbuyOffRequestList) throws Exception;
    LogBuyOffMessageModel reject(LogbuyOffRequestList logbuyOffRequestList) throws Exception;
    LogBuyOffMessageModel partial (LogbuyOffRequest logbuyOffRequest);
    LogBuyOffMessageModel skip (LogbuyOffRequest logbuyOffRequest);
    List<BuyoffLog> getListOfBuyoff(AttachmentDetailsRequest attachmentDetailsRequest);

    List<BuyoffLog> retrieveByBatchNo(String site, String batchNo);

    List<BuyoffLog> retrieveLoggedBuyOffList(String site, String pcu, String batchNo, String buyOffBO, String userId, String dateRange, LocalDateTime startDate, LocalDateTime endDate);

    BuyoffLog retrieveByBatchNoAndOrderNumberAndOperation(String site, String batchNo, String orderNumber, String operation);
}
