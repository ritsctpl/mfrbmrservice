package com.rits.pcustepstatus.service;

import com.rits.pcustepstatus.dto.*;
import com.rits.pcustepstatus.model.MessageDetails;
import com.rits.pcustepstatus.model.MessageModel;
import com.rits.pcustepstatus.model.PcuStepStatus;
import com.rits.pcustepstatus.model.PcuStepStatusDetails;

import java.util.List;

public interface PcuStepStatusService {
     List<PcuStepStatus> retrieveByPcuShopOrderProcessLot(PcuStepStatusRequest pcuStepStatusRequest) throws Exception;


    List<PcuStepStatus> getOperationDetailsByPcu(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception;

    PcuStepStatusDetails setPcuStepStatusByPcu(SetPcuStepStatusRequest setPcuStepStatusRequest)throws Exception;

    List<PcuInQueue>  getPcuInQueue(String site, String operation, String operationVersion,String shopOrder) throws Exception;

    List<PcuInWork>  getPcuInWork(String site, String operation, String operationVersion,String shopOrder) throws Exception;

    List<PcuComplete>  getPcuComplete(String site, String operation, String operationVersion,String shopOrder) throws Exception;

    List<PcuStepStatusDetails> setPcuStepStatusByShopOrder(SetPcuStepStatusRequest setPcuStepStatusRequest) throws Exception;

    Routing retrieveRouting(String site, String version, String routing)throws Exception;

    PcuHeader retrievePcuHeaderByPcu(String site, String pcuBO) throws Exception;

    List<PcuHeader> retrievePcuHeaderByShopOrder(String site, String shopOrderBO) throws Exception;


    Boolean deletePcuInQueue(String site, String pcuBo, String operationBO, String shopOrderBO)throws Exception;

    MessageModel deletePcuInWork(String site, String pcuBo, String operationBO)throws Exception;

    Boolean deletePcuComplete(String site, String pcuBo, String operationBO, String shopOrderBO, String resourceBO)throws Exception;

    MessageDetails clearPcu(String site , String operation, String operationVersion)throws Exception;

    MessageDetails placeEntireQuantityInQueue(String site, String operation, String operationVersion)throws Exception;

    MessageDetails markStepAsComplete(String site, String operation, String operationVersion)throws Exception;
//    PcuStepStatus getPcuStepStatus(String site,String pcu,String shopOrder,String type,String user) throws Exception;
}
