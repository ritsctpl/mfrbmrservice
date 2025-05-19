package com.rits.mfrservice.controller;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.AuditLogService;


import com.rits.auditlogservice.service.ProducerEvent;
import com.rits.mfrservice.Exception.MfrException;
import com.rits.mfrservice.dto.MFRResponseList;
import com.rits.mfrservice.model.MessageModel;
import com.rits.mfrservice.dto.MfrRequest;
import com.rits.mfrservice.model.Mfr;
import com.rits.mfrservice.service.MfrService;
import com.rits.mfrservice.service.MfrServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/mfr-service")
public class MfrController {

    private final MfrServiceImpl mfrServiceImpl;
    private final MfrService mfrService;
    private final ObjectMapper objectMapper;


    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?>  createMfr(@RequestBody MfrRequest mfrRequest) throws JsonProcessingException {
        MessageModel createMfr;



        try {
            createMfr = mfrService.createMfr(mfrRequest);
            return ResponseEntity.ok(createMfr);
        } catch (MfrException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/isExist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isMfrExist(@RequestBody MfrRequest mfrRequest){
        Boolean isExistResponse;
        if(mfrRequest.getSite()!=null && !mfrRequest.getSite().isEmpty()) {
            try {
                isExistResponse= mfrService.isMfrExist(mfrRequest);
                return ResponseEntity.ok(isExistResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrException(8001, mfrRequest.getSite());
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Mfr> retrieveMfr(@RequestBody MfrRequest mfrRequest) throws JsonProcessingException {
        Mfr retrieveMfrResponse;
        if(mfrRequest.getSite()!=null && !mfrRequest.getSite().isEmpty()) {


            try {


                if(mfrRequest.getSite()!=null && !mfrRequest.getSite().isEmpty()) {
                    try {
                        retrieveMfrResponse = mfrService.retrieveMfr(mfrRequest);
                        return ResponseEntity.ok(retrieveMfrResponse);
                    }catch(MfrException e){
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);

                    }
                }



            }catch(MfrException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrException(8001, mfrRequest.getSite());
    }

    @PostMapping("retrieveAll")
    public ResponseEntity<MFRResponseList> getMfrListByCreationDate(@RequestBody MfrRequest mfrRequest) {
        MFRResponseList retrieveAllMfr;
        if (mfrRequest.getSite() != null && !mfrRequest.getSite().isEmpty()) {
            try {
                retrieveAllMfr = mfrService.getMfrListByCreationDate( mfrRequest.getSite());
                return ResponseEntity.ok(retrieveAllMfr);
            } catch (MfrException mfrException) {
                throw mfrException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MfrException(1);

    }





}
