package com.rits.pcuinqueueservice.service;

import com.rits.pcuinqueueservice.dto.Extension;
import com.rits.pcuinqueueservice.dto.PcuInQueueRequest;
import com.rits.pcuinqueueservice.dto.PcuInQueueReq;
import com.rits.pcuinqueueservice.model.MessageModel;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.pcuinqueueservice.model.PcuInQueueDetails;

import java.util.List;

public interface PcuInQueueService {
    public MessageModel createPcuInQueue(PcuInQueueRequest pcuInQueueRequest)throws Exception;

    public MessageModel updatePcuInQueue(PcuInQueueRequest pcuInQueueRequest)throws Exception;
    public Boolean deletePcuInQueue(PcuInQueueReq pcuInQueueReq) throws Exception;

    List<PcuInQueue> retrieveByPcuAndSite(String pcu, String site) throws Exception;

    List<PcuInQueue> retrieveByPcuAndSiteForUnscrap(String pcu, String site) throws Exception;

    Boolean deletePcuInallOperation(String pcu, String site) throws Exception;

    Boolean unDeletePcuInallOperation(String pcu, String site) throws Exception;

    public String callExtension(Extension extension) throws Exception;
    public PcuInQueue retrievePcuInQueueAndItem(String site, String pcuBo, String item, String operation) throws Exception;
    public PcuInQueue retrievePcuInQueueAndOperation(String site, String pcuBo, String operation) throws Exception;
    public PcuInQueue retrievePcuInQueueAndResource(String site, String pcuBo, String resource,String operation) throws Exception;
    public List<PcuInQueue> retrieveListOfPcuBO(int maxRecords, String site, String operation,String resource) throws Exception;
    public List<PcuInQueue> retrieveListOfPcuBOByOperation(int maxRecords, String site, String operation) throws Exception;
    public List<PcuInQueue> retrieveListOfPcuBOByOperationAndShopOrderBO(String site, String operation,String shopOrderBO) throws Exception;
    public List<PcuInQueue> retrieveListOfPcuBOByPcu(int maxRecords, String site, String operation,String resource,String pcuBO) throws Exception;
//    public List<PcuInQueue> retrieveListOfPcuBOByPcuAndOperation(String site, String operation, String pcuBO) throws Exception;
    public List<PcuInQueue> getRecByPcuandRout(PcuInQueueRequest pcuInQueueRequest);
    public MessageModel updateAllPcu(PcuInQueueRequest pcuInQueueRequest)throws Exception;
    List<PcuInQueue> retrieveAllPcuBySite(String site);
    PcuInQueueRequest convertToPcuInQueueRequest(PcuInQueueReq pcuInQueueReq);
    PcuInQueueDetails convertToPcuInQueueNoBO(PcuInQueue pcuInQueue);
    List<PcuInQueueDetails> convertToPcuInQueueNoBOAsList(List<PcuInQueue> responseList);

    String getOperationCurrentVer(PcuInQueueReq pcuInQueueReq) throws Exception;
}
