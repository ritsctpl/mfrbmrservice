package com.rits.customdataformatservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.customdataformatservice.dto.CustomDataFormatObject;
import com.rits.customdataformatservice.dto.CustomDataFormatRequest;
import com.rits.customdataformatservice.dto.CustomDataFormatResponseList;
import com.rits.customdataformatservice.exception.CustomDataFormatException;
import com.rits.customdataformatservice.model.*;
import com.rits.customdataformatservice.model.MessageModel;
import com.rits.customdataformatservice.service.CustomDataFormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/customdataformat-service")
public class CustomDataFormatController {
    private final CustomDataFormatService customDataFormatService;

    @PostMapping("create")
    public ResponseEntity<MessageModel> createCustomDataFormat(@RequestBody CustomDataFormatRequest customDataFormatRequest) throws Exception {
        MessageModel createCustomDataFormat;
        try {
            createCustomDataFormat = customDataFormatService.createCustomDataFormat(customDataFormatRequest);
            return ResponseEntity.ok(createCustomDataFormat);
        } catch (CustomDataFormatException customDataFormatException) {
            throw customDataFormatException;
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("retrieve")
    public ResponseEntity<CustomDataFormat> retrieveCustomDataFormat(@RequestBody CustomDataFormatRequest customDataFormatRequest) throws Exception {
        if (customDataFormatRequest.getSite() != null && !customDataFormatRequest.getSite().isEmpty()) {
            CustomDataFormat retrieveCustomDataFormat;

            try {
                retrieveCustomDataFormat = customDataFormatService.retrieveCustomDataFormat(customDataFormatRequest.getSite(), customDataFormatRequest.getCode(), customDataFormatRequest.getSequence());
                return ResponseEntity.ok(retrieveCustomDataFormat);
            } catch (CustomDataFormatException customDataFormatException) {
                throw customDataFormatException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CustomDataFormatException(1);
    }

    @PostMapping("retrieveTop50")
    public ResponseEntity<CustomDataFormatResponseList> getCustomDataFormat(@RequestBody CustomDataFormatRequest customDataFormatRequestRequest) {
        if (customDataFormatRequestRequest.getSite() != null && !customDataFormatRequestRequest.getSite().isEmpty()) {
            CustomDataFormatResponseList retrieveTop50CustomDataFormat;
            try {
                retrieveTop50CustomDataFormat = customDataFormatService.getAllCustomDataFormat(customDataFormatRequestRequest.getSite());
                return ResponseEntity.ok(retrieveTop50CustomDataFormat);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CustomDataFormatException(1);
    }

    @PostMapping("isDataFormatPresent")

    public CustomDataFormatObject isDataFormatPresent(@RequestBody DataFormatRequest dataFormatRequest) {
        if (dataFormatRequest.getSite() != null && !dataFormatRequest.getSite().isEmpty()) {
            List<MainCustomDataObject> isDataFormatPresent;
            try {
                isDataFormatPresent = customDataFormatService.isDataFormatPresent(dataFormatRequest);
                return CustomDataFormatObject.builder().mainCustomDataObjectList(isDataFormatPresent).build();
            } catch (CustomDataFormatException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new CustomDataFormatException(1);
    }

}


