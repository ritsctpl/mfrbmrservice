package com.rits.batchnoheader.service;

import com.rits.batchnoheader.dto.BatchNoHeaderRequest;
import com.rits.batchnoheader.model.BatchNoHeader;
import com.rits.batchnoheader.model.BatchNoHeaderMsgModel;

public interface BatchNoHeaderService {
    public BatchNoHeaderMsgModel create(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel update(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel delete(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel retrieve(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel retrieveAll(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel retrieveBatchNoList(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public BatchNoHeaderMsgModel retrieveTop50(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;
    public boolean isBatchNoHeaderExist(String site, String batchNo) throws Exception;

    boolean isBatchNoHeader(BatchNoHeaderRequest batchNoHeaderRequest) throws Exception;

    BatchNoHeader getBySiteAndBatchNoAndOrderNoAndMaterialAndMaterialVersion(String site, String batchNo, String orderNo, String material, String materialVersion);
    BatchNoHeader getBySiteAndBatchNumber(String site, String batchNo);
    BatchNoHeader getBySiteAndBatchNo(String site, String batchNo);
    public BatchNoHeaderMsgModel retrieveOnlyBatchNumberList(BatchNoHeaderRequest batchNoHeaderRequest);
}
