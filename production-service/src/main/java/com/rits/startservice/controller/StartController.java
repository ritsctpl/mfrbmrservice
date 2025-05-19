package com.rits.startservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.startservice.dto.*;
import com.rits.pcuinqueueservice.exception.PcuInQueueException;
import com.rits.startservice.exception.StartException;
import com.rits.startservice.model.MessageModel;
import com.rits.startservice.model.PcuInWorkMessageModel;
import com.rits.startservice.service.StartService;
import com.rits.startservice.service.StartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/start-service")
public class StartController {
    private final StartService startService;
    private final StartServiceImpl startServiceImpl;
    private final ObjectMapper objectMapper;

    @PostMapping("create")
    public ResponseEntity<?> createPcuInWork(@RequestBody JsonNode payload) throws JsonProcessingException {
        PcuInWorkMessageModel createPcuInWork;
        StartRequestDetails createStartRequest = new ObjectMapper().convertValue(payload, StartRequestDetails.class);

        try {
            createPcuInWork = startService.createPcuInWork(createStartRequest);
            return ResponseEntity.ok( PcuInWorkMessageModel.builder().message_details(createPcuInWork.getMessage_details()).response(createPcuInWork.getResponse()).build());

        } catch (StartException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("update")
    public ResponseEntity<PcuInWorkMessageModel> updatePcuInWork(@RequestBody StartRequestDetails startRequest) throws JsonProcessingException {
        PcuInWorkMessageModel updatePcuInWork;

        try {
            if(startRequest.getDisable() != null && startRequest.getDisable()){
                updatePcuInWork = startService.updateAllPcu(startRequest);
                return ResponseEntity.ok( PcuInWorkMessageModel.builder().message_details(updatePcuInWork.getMessage_details()).response(updatePcuInWork.getResponse()).build());

            }
            updatePcuInWork = startService.updatePcuInWork(startRequest);
            return ResponseEntity.ok( PcuInWorkMessageModel.builder().message_details(updatePcuInWork.getMessage_details()).response(updatePcuInWork.getResponse()).build());

        } catch (StartException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    public ResponseEntity<StartRequestDetails> retrievePcuInWork(@RequestBody StartRequestDetails startRequest) throws Exception {

        startRequest.setOperationVersion(startServiceImpl.getOperationCurrentVer(startRequest));
        StartRequestDetails response = null;
        try {
            if((startRequest.getOperation()!=null && !startRequest.getOperation().isEmpty()&& (startRequest.getItem()==null || startRequest.getItem().isEmpty())&&(startRequest.getResource()==null || startRequest.getResource().isEmpty()))){
                response = startService.retrievePcuInWorkByOperation(startRequest);
            }
            if(startRequest.getItem()!=null && !startRequest.getItem().isEmpty()&&(startRequest.getResource()==null || startRequest.getResource().isEmpty())){
                response = startService.retrievePcuInWorkByOperationAndItem(startRequest);
            }
            if(startRequest.getResource()!=null && !startRequest.getResource().isEmpty()  && (startRequest.getItem()==null || startRequest.getItem().isEmpty())){
                response = startService.retrievePcuInWorkByOperationAndResource(startRequest);
            }
            return ResponseEntity.ok(response);
        } catch (StartException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveByPcuAndSite")
    public List<StartRequestDetails> retrieveByPcuAndSite(@RequestBody StartRequestDetails startRequest)
    {
        try {
           return startService.retrieveByPcuAndSite(startRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveDeletedPcu")
    public List<StartRequestDetails> retrieveDeletedPcu(@RequestBody StartRequestDetails startRequest)
    {
        try {
            return startService.retrieveDeletedPcu(startRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("deletePcuInAllOperations")
    public Boolean deletePcuInAllOperations(@RequestBody StartRequestDetails startRequest)
    {
        try {
            return startService.deletePcuFromAllOperations(startRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("unDeletePcuInAllOperations")
    public Boolean unDeletePcuInAllOperations(@RequestBody StartRequestDetails startRequest)
    {
        try {
            return startService.unDeletePcuFromAllOperations(startRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("delete")
    public MessageModel deletePcuInWork(@RequestBody StartRequestDetails startRequest) throws JsonProcessingException {

        try {
            MessageModel deletedPcuInWork = startService.deletePcuInWork(startRequest);
            return deletedPcuInWork;

        } catch (StartException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByOperation")
    public List<StartRequestDetails> retrieveByOperation(@RequestBody StartRequestDetails startRequest)
    {
        try {
          return startService.retrieveListByOperation(startRequest);
        } catch (StartException e) {
            throw  e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("retrieveByOperationAndResource")
   public List<StartRequestDetails> retrieveByOperationAndResource(@RequestBody StartRequestDetails startRequest)// need to remove
    {
        try {
            return startService.retrieveListByOperationAndResource(startRequest);
        } catch (StartException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveAll")
    public PcuList retrieveAll(@RequestBody StartRequestDetails startRequest)
    {
        try {
            return startService.retrieveAll(startRequest);
        } catch (StartException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public MessageModel start(@RequestBody StartRequestLists startRequestList)
    {
        try {
            return startService.start(startRequestList);
        } catch (StartException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveListPCUOfPcuBO")
    public ResponseEntity<List<StartRequestDetails>> retrieveListOfPcuBO(@RequestBody StartRequestDetails startRequest) throws Exception {
        List<StartRequestDetails> retrieveListOfPcus= new ArrayList<>();

        try {
            if((startRequest.getOperation()!=null && !startRequest.getOperation().isEmpty())&&(startRequest.getResource()!=null&& !startRequest.getResource().isEmpty())&&(startRequest.getPcu()==null||startRequest.getPcu().isEmpty())) {
                retrieveListOfPcus = startService.retrieveListByOperationAndResource(startRequest);
            }else if((startRequest.getOperation()!=null && !startRequest.getOperation().isEmpty())&&(startRequest.getResource()==null || startRequest.getResource().isEmpty())&&(startRequest.getPcu()==null||startRequest.getPcu().isEmpty())) {
                retrieveListOfPcus = startService.retrieveListByOperation(startRequest);
            }
            return ResponseEntity.ok(retrieveListOfPcus);
        } catch (PcuInQueueException pcuInQueueException) {
            throw  pcuInQueueException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getAllPcuByRoute")
    public List<StartRequestDetails> getAllPcuByRoute(@RequestBody StartRequestDetails startRequest){
        return startService.getAllPcuByRoute(startRequest);
    }

    @PostMapping("getAllActiveAndInQueuePcuBySite")
    public List<StartRequestDetails> getAllInQueuePcuBySite(@RequestBody StartRequestDetails startRequest){
        return startService.getAllInQueuePcuBySite(startRequest.getSite());
    }
}
