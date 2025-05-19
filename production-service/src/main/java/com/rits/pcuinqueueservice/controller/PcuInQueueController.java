package com.rits.pcuinqueueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.pcuinqueueservice.dto.Extension;
import com.rits.pcuinqueueservice.dto.PcuInQueueRequest;
import com.rits.pcuinqueueservice.dto.PcuInQueueReq;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.pcuinqueueservice.model.MessageModel;
import com.rits.pcuinqueueservice.model.PcuInQueue;
import com.rits.pcuinqueueservice.model.PcuInQueueDetails;
import com.rits.pcuinqueueservice.service.PcuInQueueService;

import com.rits.startservice.service.StartService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/pcuinqueue-service")
public class PcuInQueueController {
    private final PcuInQueueService pcuInQueueService;

    private final StartService startService;

    private final ObjectMapper objectMapper;

    @PostMapping("create")
    public ResponseEntity<?> createPcuInQueue(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        MessageModel createPcuInQueue;

        try {
            PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
            createPcuInQueue = pcuInQueueService.createPcuInQueue(pcuInQueueRequest);

            return ResponseEntity.ok( MessageModel.builder().message_details(createPcuInQueue.getMessage_details()).response(createPcuInQueue.getResponse()).build());
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public ResponseEntity<MessageModel> updatePcuInQueue(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        MessageModel updatePcuInQueue;
        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(pcuInQueueReq.getSite()).hookPoint("PRE").activity("pcuInQueue-service").hookableMethod("update").request(objectMapper.writeValueAsString(pcuInQueueRequest)).build();
        String preExtensionResponse = pcuInQueueService.callExtension(preExtension);
        PcuInQueueRequest preExtensionPcuInQueueRequest = objectMapper.readValue(preExtensionResponse, PcuInQueueRequest.class);

        try {
            if(pcuInQueueRequest.getDisable()){
                updatePcuInQueue= pcuInQueueService.updateAllPcu(pcuInQueueRequest);
                return ResponseEntity.ok( MessageModel.builder().message_details(updatePcuInQueue.getMessage_details()).response(updatePcuInQueue.getResponse()).build());

            }
            updatePcuInQueue = pcuInQueueService.updatePcuInQueue(preExtensionPcuInQueueRequest);
            return ResponseEntity.ok( MessageModel.builder().message_details(updatePcuInQueue.getMessage_details()).response(updatePcuInQueue.getResponse()).build());

        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public ResponseEntity<Boolean> deletePcuInQueue(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        Boolean deletePcuInQueue;

        try {
            deletePcuInQueue = pcuInQueueService.deletePcuInQueue(pcuInQueueReq);

            return ResponseEntity.ok( deletePcuInQueue);

        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public ResponseEntity<PcuInQueueDetails> retrievePcuInQueue(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        if(StringUtils.isEmpty(pcuInQueueReq.getOperationVersion())){
            pcuInQueueReq.setOperationVersion(pcuInQueueService.getOperationCurrentVer(pcuInQueueReq));
        }

        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        PcuInQueue retrievePcuInQueue=null;
//        objectMapper.registerModule(new JavaTimeModule());
//        Extension preExtension = Extension.builder().site(pcuInQueueRequest.getSite()).hookPoint("PRE").activity("pcuInQueue-service").hookableMethod("update").request(objectMapper.writeValueAsString(pcuInQueueRequest)).build();
//        String preExtensionResponse = pcuInQueueService.callExtension(preExtension);
//        PcuInQueueRequest preExtensionPcuInQueueRequest = objectMapper.readValue(preExtensionResponse, PcuInQueueRequest.class);
        try {
            if((pcuInQueueRequest.getOperationBO()!=null && !pcuInQueueRequest.getOperationBO().isEmpty()&& (pcuInQueueRequest.getItemBO()==null || pcuInQueueRequest.getItemBO().isEmpty())&&(pcuInQueueRequest.getResourceBO()==null || pcuInQueueRequest.getResourceBO().isEmpty()))){
                retrievePcuInQueue = pcuInQueueService.retrievePcuInQueueAndOperation(pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getOperationBO());
            }
            if(pcuInQueueRequest.getItemBO()!=null && !pcuInQueueRequest.getItemBO().isEmpty()&&(pcuInQueueRequest.getResourceBO()==null || pcuInQueueRequest.getResourceBO().isEmpty())){
                retrievePcuInQueue = pcuInQueueService.retrievePcuInQueueAndItem(pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getItemBO(), pcuInQueueRequest.getOperationBO());
            }
            if(pcuInQueueRequest.getResourceBO()!=null && !pcuInQueueRequest.getResourceBO().isEmpty()  && (pcuInQueueRequest.getItemBO()==null || pcuInQueueRequest.getItemBO().isEmpty())){
                retrievePcuInQueue = pcuInQueueService.retrievePcuInQueueAndResource(pcuInQueueRequest.getSite(), pcuInQueueRequest.getPcuBO(), pcuInQueueRequest.getResourceBO(), pcuInQueueRequest.getOperationBO());
            }

            PcuInQueueDetails pcuInQueueNoBO = pcuInQueueService.convertToPcuInQueueNoBO(retrievePcuInQueue);
            return ResponseEntity.ok(pcuInQueueNoBO);
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveListOfPcuBO")
    public ResponseEntity<List<PcuInQueueDetails>> retrieveListOfPcuBO(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        if(StringUtils.isEmpty(pcuInQueueReq.getOperationVersion())){
            pcuInQueueReq.setOperationVersion(pcuInQueueService.getOperationCurrentVer(pcuInQueueReq));
        }

        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        List<PcuInQueue> retrieveListOfPcuBO= new ArrayList<>();
        int maxRecords = pcuInQueueReq.getRecordLimit();

        try {
            if((pcuInQueueRequest.getOperationBO()!=null && !pcuInQueueRequest.getOperationBO().isEmpty())&&(pcuInQueueRequest.getResourceBO()!=null&& !pcuInQueueRequest.getResourceBO().isEmpty())&&(pcuInQueueRequest.getPcuBO()==null||pcuInQueueRequest.getPcuBO().isEmpty())) {
                retrieveListOfPcuBO = pcuInQueueService.retrieveListOfPcuBO(maxRecords, pcuInQueueRequest.getSite(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getResourceBO());
            }else if((pcuInQueueRequest.getOperationBO()!=null && !pcuInQueueRequest.getOperationBO().isEmpty())&&(pcuInQueueRequest.getResourceBO()==null || pcuInQueueRequest.getResourceBO().isEmpty())&&(pcuInQueueRequest.getPcuBO()==null||pcuInQueueRequest.getPcuBO().isEmpty())) {
                retrieveListOfPcuBO = pcuInQueueService.retrieveListOfPcuBOByOperation(maxRecords, pcuInQueueRequest.getSite(), pcuInQueueRequest.getOperationBO());
            }
            else if((pcuInQueueRequest.getPcuBO()!=null&&!pcuInQueueRequest.getPcuBO().isEmpty())&&(pcuInQueueRequest.getOperationBO()!=null && !pcuInQueueRequest.getOperationBO().isEmpty())&&(pcuInQueueRequest.getResourceBO()!=null&& !pcuInQueueRequest.getResourceBO().isEmpty())){
                retrieveListOfPcuBO=pcuInQueueService.retrieveListOfPcuBOByPcu(maxRecords, pcuInQueueRequest.getSite(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getResourceBO(),pcuInQueueRequest.getPcuBO());
            }
            List<PcuInQueueDetails> filteredList = pcuInQueueService.convertToPcuInQueueNoBOAsList(retrieveListOfPcuBO);
            return ResponseEntity.ok(filteredList);
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getAllPcuByRoute")
    public List<PcuInQueueDetails> getAllPcuByRoute(@RequestBody PcuInQueueReq pcuInQueueReq){
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);

        List<PcuInQueue> getAllPcuByRoute= new ArrayList<>();
        getAllPcuByRoute = pcuInQueueService.getRecByPcuandRout(pcuInQueueRequest);

        List<PcuInQueueDetails> filteredList = pcuInQueueService.convertToPcuInQueueNoBOAsList(getAllPcuByRoute);
        return filteredList;
    }

    @PostMapping("retrieveByPcu")
    public List<PcuInQueueDetails> getByPcuAndSite(@RequestBody PcuInQueueReq pcuInQueueReq){
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        try {
            List<PcuInQueue> getByPcuAndSite= new ArrayList<>();
            getByPcuAndSite = pcuInQueueService.retrieveByPcuAndSite(pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getSite());

            List<PcuInQueueDetails> filteredList = pcuInQueueService.convertToPcuInQueueNoBOAsList(getByPcuAndSite);
            return filteredList;
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveDeletedPcu")
    public List<PcuInQueueDetails> getDeletedPcu(@RequestBody PcuInQueueReq pcuInQueueReq){
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        try {
            List<PcuInQueue> getDeletedPcu= new ArrayList<>();
            getDeletedPcu = pcuInQueueService.retrieveByPcuAndSiteForUnscrap(pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getSite());

            return pcuInQueueService.convertToPcuInQueueNoBOAsList(getDeletedPcu);
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("deletePcuInAllOperations")
    public Boolean deletePcuInAllOperations(@RequestBody PcuInQueueReq pcuInQueueReq){
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        try {
            return pcuInQueueService.deletePcuInallOperation(pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getSite());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("unDeletePcuInAllOperations")
    public Boolean unDeletePcuInAllOperations(@RequestBody PcuInQueueReq pcuInQueueReq){
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);
        try {
            return pcuInQueueService.unDeletePcuInallOperation(pcuInQueueRequest.getPcuBO(),pcuInQueueRequest.getSite());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveListOfPcuBOByOperationAndShopOrder")
    public List<PcuInQueueDetails> retrieveListOfPcuBOByOperationAndShopOrder(@RequestBody PcuInQueueReq pcuInQueueReq) throws Exception {
        PcuInQueueRequest pcuInQueueRequest = pcuInQueueService.convertToPcuInQueueRequest(pcuInQueueReq);

        List<PcuInQueue> retrieveListOfPcuBOByOperationAndShopOrder= new ArrayList<>();
        retrieveListOfPcuBOByOperationAndShopOrder = pcuInQueueService.retrieveListOfPcuBOByOperationAndShopOrderBO(pcuInQueueRequest.getSite(), pcuInQueueRequest.getOperationBO(), pcuInQueueRequest.getShopOrderBO());

        List<PcuInQueueDetails> filteredList = pcuInQueueService.convertToPcuInQueueNoBOAsList(retrieveListOfPcuBOByOperationAndShopOrder);
        return filteredList;
    }

    @PostMapping("retrieveAllBySite")
    public List<PcuInQueueDetails> retrieveListOfPcuBOSite(@RequestBody PcuInQueueRequest pcuInQueueRequest) throws Exception {

        List<PcuInQueue> retrieveListOfPcuBOSite = pcuInQueueService.retrieveAllPcuBySite(pcuInQueueRequest.getSite());

//        List<PcuInQueueNoBO> filteredList = retrieveListOfPcuBOSite.stream()
//                .map(PcuInQueueNoBO::new)
//                .collect(Collectors.toList());
//        return filteredList;
        List<PcuInQueueDetails> filteredList =  pcuInQueueService.convertToPcuInQueueNoBOAsList(retrieveListOfPcuBOSite);
        return filteredList;
    }
}
