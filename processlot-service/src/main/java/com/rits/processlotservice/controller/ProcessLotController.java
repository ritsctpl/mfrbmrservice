package com.rits.processlotservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.processlotservice.dto.*;
import com.rits.processlotservice.exception.ProcessLotException;
import com.rits.processlotservice.model.EmployeeMessageModel;
import com.rits.processlotservice.model.MessageModel;
import com.rits.processlotservice.model.ProcessLot;
import com.rits.processlotservice.service.ProcessLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/processlot-service")
public class ProcessLotController {
    private final ProcessLotService processLotService;
    private final ObjectMapper objectMapper;

    //{
    //    "site":"rits",
    //    "processLot":"processLot",
    //    "processLotMember": [
    //    {
    //      "pcuBO": "pcuBO1"
    //    }
    //  ],
    //  "createdBy": "priya"
    //}
    @PostMapping("create")
    public ResponseEntity<?> createProcessLot(@RequestBody ProcessLotRequest processLotRequest) throws Exception {
        MessageModel createProcessLot;
//        MessageModel validationResponse = processLotService.validation( payload);
//
//        if (validationResponse.getMessage_details().getMsg_type().equals("E")) {
//            return ResponseEntity.badRequest().body(validationResponse.getMessage_details().getMsg());
//        }
//        ProcessLotRequest processLotRequest = objectMapper.convertValue(payload, ProcessLotRequest.class);

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(processLotRequest.getSite()).hookPoint("PRE").activity("processlot-service").hookableMethod("create").request(objectMapper.writeValueAsString(processLotRequest)).build();
        String preExtensionResponse = processLotService.callExtension(preExtension);
        ProcessLotRequest preExtensionProcessLotRequest = objectMapper.readValue(preExtensionResponse, ProcessLotRequest.class);

        try {
            createProcessLot = processLotService.createProcessLot(preExtensionProcessLotRequest);
            Extension postExtension = Extension.builder().site(processLotRequest.getSite()).hookPoint("POST").activity("processlot-service").hookableMethod("create").request(objectMapper.writeValueAsString(createProcessLot.getResponse())).build();
            String postExtensionResponse = processLotService.callExtension(postExtension);
            ProcessLot postExtensionProcessLot = objectMapper.readValue(postExtensionResponse, ProcessLot.class);
            return ResponseEntity.ok( MessageModel.builder().message_details(createProcessLot.getMessage_details()).response(postExtensionProcessLot).build());

        } catch (ProcessLotException processLotException) {
            throw processLotException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //{
    //    "site":"rits",
    //    "processLot":"processLot"
    //}
    @PostMapping("retrievePcuByProcessLot")
    public ResponseEntity<?> retrievePcuByProcessLot(@RequestBody ProcessLotRequest processLotRequest) throws Exception {
        ProcessLotResponse retrievePcuByProcessLot;

        if(processLotRequest.getProcessLot()==null && processLotRequest.getSite().isEmpty()){
            throw new ProcessLotException(1);
        }

        try {
            retrievePcuByProcessLot = processLotService.retrievePcuByProcessLot(processLotRequest.getSite(), processLotRequest.getProcessLot());
            return ResponseEntity.ok( retrievePcuByProcessLot);
        } catch (ProcessLotException processLotException) {
            throw processLotException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @PostMapping("/createEmp")
    public ResponseEntity<?> createHrmUser(@RequestBody EmployeeRequest employeeRequest)
    {
        try
        {
            if(employeeRequest==null){
             throw new ProcessLotException(422);
            }
            EmployeeMessageModel response = processLotService.createHrmUser(employeeRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ProcessLotException processLotException) {
            throw processLotException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateHrmUser(@RequestBody EmployeeRequest employeeRequest)
    {
        try
        {
            if(employeeRequest==null){
                throw new ProcessLotException(422);
            }
            EmployeeMessageModel response = processLotService.updateHrmUser(employeeRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ProcessLotException processLotException) {
            throw processLotException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/retrieveAll")
    public ResponseEntity<EmployeeResponseList> getEmployeeList(@RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponseList employeeResponseList;
        if (employeeRequest.getSite() != null && !employeeRequest.getSite().isEmpty()) {
            try {
                employeeResponseList = processLotService.retrieveAll(employeeRequest.getSite());
                return ResponseEntity.ok(employeeResponseList);
            } catch (ProcessLotException processLotException) {
                throw processLotException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ProcessLotException(422);
    }



}

