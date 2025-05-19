package com.rits.pcudoneservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.pcudoneservice.dto.Extension;
import com.rits.pcudoneservice.dto.PcuDoneRequest;
import com.rits.pcudoneservice.dto.PcuDoneRequestNoBO;
import com.rits.pcudoneservice.exception.PcuDoneException;
import com.rits.pcudoneservice.model.MessageModel;
import com.rits.pcudoneservice.model.PcuDone;
import com.rits.pcudoneservice.model.PcuDoneNoBO;
import com.rits.pcudoneservice.service.PcuDoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/pcudone-service")
public class PcuDoneController {
    private final PcuDoneService pcuDoneService;
    private final ObjectMapper objectMapper;
    @PostMapping("insert")
    public ResponseEntity<?> insert(@RequestBody PcuDoneRequestNoBO pcuDoneRequestNoBO) throws Exception {
        PcuDoneRequest pcuDoneRequest = pcuDoneService.convertToPcuDoneRequest(pcuDoneRequestNoBO);
        MessageModel insert;

//        objectMapper.registerModule(new JavaTimeModule());
//        Extension preExtension = Extension.builder().site(pcuDoneRequest.getSite()).hookPoint("PRE").activity("pcuDone-service").hookableMethod("create").request(objectMapper.writeValueAsString(pcuDoneRequest)).build();
//        String preExtensionResponse = pcuDoneService.callExtension(preExtension);
//        PcuDoneRequest preExtensionPcuDoneRequest = objectMapper.readValue(preExtensionResponse, PcuDoneRequest.class);
        try {

            insert = pcuDoneService.insert(pcuDoneRequest);
//            Extension postExtension = Extension.builder().site(pcuDoneRequest.getSite()).hookPoint("POST").activity("pcuDone-service").hookableMethod("create").request(objectMapper.writeValueAsString(insert.getResponse())).build();
//            String postExtensionResponse = pcuDoneService.callExtension(postExtension);
//            PcuDoneNoBO postExtensionPcuComplete = objectMapper.readValue(postExtensionResponse, PcuDoneNoBO.class);
            return ResponseEntity.ok(MessageModel.builder().message_details(insert.getMessage_details()).response(insert.getResponse()).build());
        } catch (PcuDoneException pcuDoneException) {
            throw pcuDoneException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("deleteByPcu")
    public Boolean deleteByPcu(@RequestBody PcuDoneRequestNoBO pcuDoneRequestNoBO)
    {
        PcuDoneRequest pcuDoneRequest = pcuDoneService.convertToPcuDoneRequest(pcuDoneRequestNoBO);
        return pcuDoneService.delete(pcuDoneRequest.getSite(),pcuDoneRequest.getPcuBO());
    }
    @PostMapping("unDeleteByPcu")
    public Boolean unDeleteByPcu(@RequestBody PcuDoneRequestNoBO pcuDoneRequestNoBO)
    {
        PcuDoneRequest pcuDoneRequest = pcuDoneService.convertToPcuDoneRequest(pcuDoneRequestNoBO);
        return pcuDoneService.unDelete(pcuDoneRequest.getSite(),pcuDoneRequest.getPcuBO());
    }
    @PostMapping("retrieve")
    public PcuDoneNoBO retrieve(@RequestBody PcuDoneRequestNoBO pcuDoneRequestNoBO)
    {
        try {
           PcuDoneRequest pcuDoneRequest = pcuDoneService.convertToPcuDoneRequest(pcuDoneRequestNoBO);
           PcuDone pcuDone = pcuDoneService.retrieve(pcuDoneRequest.getSite(), pcuDoneRequest.getPcuBO());
           PcuDoneNoBO retrievedPcuDone = pcuDoneService.convertToPcuDoneNoBO(pcuDone);
           return retrievedPcuDone;
        }catch (PcuDoneException e){
            throw e;
        }catch(Exception e){
            throw e;
        }
    }

}
