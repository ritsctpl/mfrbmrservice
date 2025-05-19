package com.rits.batchnoyieldreportingservice.service;

import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.model.BatchNoYieldReporting;
import com.rits.batchnoyieldreportingservice.model.MessageModel;

import java.util.List;

public interface BatchNoYieldReportingService {

    MessageModel create(BatchNoYieldReportingRequest request);

    MessageModel update(BatchNoYieldReportingRequest request);

    MessageModel delete(BatchNoYieldReportingRequest request);

    BatchNoYieldReporting retrieve(BatchNoYieldReportingRequest request);


    List<BatchNoYieldReporting> retrieveAll(String site);

    List<BatchNoYieldReporting> retrieveTop50(String site);

    boolean isBatchNoYieldReportingExist(String site, String batchNo);
}
