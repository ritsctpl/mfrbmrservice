package com.rits.bomheaderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.bomheaderservice.dto.BomComponentList;
import com.rits.bomheaderservice.dto.BomComponentListRequest;
import com.rits.bomheaderservice.dto.BomHeaderRequest;
import com.rits.bomheaderservice.dto.Extension;
import com.rits.bomheaderservice.model.Bom;
import com.rits.bomheaderservice.model.BomHeader;
import com.rits.bomheaderservice.model.BomHeaderMessageModel;
import com.rits.bomheaderservice.service.BomHeaderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/bomheader-service")
public class BomHeaderController {

    private final BomHeaderServiceImpl bomHeaderServiceImpl;
    private final ObjectMapper objectMapper;

    @RequestMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody BomHeaderRequest bomHeaderRequest) throws JsonProcessingException {
        BomHeaderMessageModel createBomHeader;

        try {
            createBomHeader = bomHeaderServiceImpl.create(bomHeaderRequest);
            return ResponseEntity.ok( BomHeaderMessageModel.builder().message_details(createBomHeader.getMessage_details()).response(createBomHeader.getResponse()).build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }
    @RequestMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> update(@RequestBody BomHeaderRequest bomHeaderRequest) throws JsonProcessingException {
        BomHeaderMessageModel updateBomHeader;

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(bomHeaderRequest.getBom().getSite()).hookPoint("PRE").activity("bomheader-service").hookableMethod("create").request(objectMapper.writeValueAsString(bomHeaderRequest)).build();
        String preExtensionResponse = bomHeaderServiceImpl.callExtension(preExtension);
        BomHeaderRequest preExtensionBomHeader = objectMapper.readValue(preExtensionResponse, BomHeaderRequest.class);

        try {
            updateBomHeader = bomHeaderServiceImpl.update(preExtensionBomHeader);
            Extension postExtension = Extension.builder().site(bomHeaderRequest.getBom().getSite()).hookPoint("POST").activity("bomheader-service").hookableMethod("create").request(objectMapper.writeValueAsString(updateBomHeader.getResponse())).build();
            String postExtensionResponse = bomHeaderServiceImpl.callExtension(postExtension);
            BomHeader postExtensionUserGroup = objectMapper.readValue(postExtensionResponse, BomHeader.class);
            return ResponseEntity.ok( BomHeaderMessageModel.builder().message_details(updateBomHeader.getMessage_details()).response(postExtensionUserGroup).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @RequestMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Bom> retrieve(@RequestBody BomHeaderRequest bomHeaderRequest) throws JsonProcessingException {
        Bom retrieveBomHeader;

        objectMapper.registerModule(new JavaTimeModule());
        Extension preExtension = Extension.builder().site(bomHeaderRequest.getSite()).hookPoint("PRE").activity("bomheader-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(bomHeaderRequest)).build();
        String preExtensionResponse = bomHeaderServiceImpl.callExtension(preExtension);
        BomHeaderRequest preExtensionBomHeader = objectMapper.readValue(preExtensionResponse, BomHeaderRequest.class);

        try {
            retrieveBomHeader = bomHeaderServiceImpl.retrieve(preExtensionBomHeader);
            Extension postExtension = Extension.builder().site(bomHeaderRequest.getSite()).hookPoint("POST").activity("bomheader-service").hookableMethod("retrieve").request(objectMapper.writeValueAsString(retrieveBomHeader)).build();
            String postExtensionResponse = bomHeaderServiceImpl.callExtension(postExtension);
            Bom postExtensionBomHeader = objectMapper.readValue(postExtensionResponse, Bom.class);
            return ResponseEntity.ok(postExtensionBomHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/updateStatus" )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BomHeader>> updateStatusOfBomToReleased(@RequestBody BomHeaderRequest bomHeaderRequest)
    {

        try {
            List<BomHeader> updatedRecord = bomHeaderServiceImpl.updateStatusOfBomToReleased(bomHeaderRequest);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/deleteByPcu" )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> deleteBomHeaderByPcu(@RequestBody BomHeaderRequest bomHeaderRequest)
    {

        try {
            Boolean deleteBomHeader = bomHeaderServiceImpl.deleteBomHeaderByPcu(bomHeaderRequest.getSite(),bomHeaderRequest.getPcuBO());
            return ResponseEntity.ok(deleteBomHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @RequestMapping("/unDeleteByPcu" )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> unDeleteBomHeaderByPcu(@RequestBody BomHeaderRequest bomHeaderRequest)
    {

        try {
            Boolean deleteBomHeader = bomHeaderServiceImpl.unDeleteBomHeaderByPcu(bomHeaderRequest.getSite(),bomHeaderRequest.getPcuBO());
            return ResponseEntity.ok(deleteBomHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @RequestMapping("/getComponentList")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BomComponentList> getComponentList(@RequestBody BomComponentListRequest bomComponentListRequest)
    {
        BomComponentList bomComponentList;

        try {
            bomComponentList = bomHeaderServiceImpl.getComponentListByOperation(bomComponentListRequest.getSite(), bomComponentListRequest.getBom(), bomComponentListRequest.getRevision(), bomComponentListRequest.getPcuBO(), bomComponentListRequest.getOperation());
            return ResponseEntity.ok(bomComponentList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
