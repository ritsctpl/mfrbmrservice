package com.rits.nonconformanceservice.service;

import com.rits.listmaintenceservice.dto.ListMaintenanceResponseList;
import com.rits.nonconformanceservice.dto.DispositionRequest;
import com.rits.nonconformanceservice.dto.DispositionRoutings;
import com.rits.nonconformanceservice.dto.NcRequest;
import com.rits.nonconformanceservice.exception.NonConformanceserviceException;
import com.rits.nonconformanceservice.model.NcData;

import java.util.List;

public interface NonConformanceservice {
    List<DispositionRoutings> logNc(NcRequest ncRequest) throws Exception;

    List<NcData> getNcData(String PCUBo, String OperationBO, String ResourceBO);

    Boolean closeNC(NcRequest ncRequest);

    List<NcData> getAllNcByPCU(String PCUBO);

    Boolean donePCU(DispositionRequest dispositionRequest) throws NonConformanceserviceException;

    List<DispositionRoutings> getDispositionRouting(List<NcData> ncRequest);

    List<NcData> retrieveBySiteAndPcu(String site, String pcu) throws Exception;
}
