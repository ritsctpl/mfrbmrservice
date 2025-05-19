package com.rits.mfrscreenconfigurationservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.auditlogservice.dto.AuditLogRequest;
import com.rits.auditlogservice.service.ProducerEvent;

import com.rits.mfrscreenconfigurationservice.dto.MfrScreenConfigurationRequest;
import com.rits.mfrscreenconfigurationservice.dto.ProductResponseList;
import com.rits.mfrscreenconfigurationservice.exception.MFRScreenConfigurationException;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;
import com.rits.mfrscreenconfigurationservice.model.MessageModel;
import com.rits.mfrscreenconfigurationservice.service.MFRScreenConfigurationService;
import com.rits.mfrscreenconfigurationservice.service.MFRScreenConfigurationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/mfrscreenconfiguration-service")
public class MFRScreenConfigurationController {
    private final MFRScreenConfigurationService mfrScreenConfigurationService;
    private final MFRScreenConfigurationServiceImpl mfrScreenConfigurationServiceImpl;
    @PostMapping("create")
    public ResponseEntity<?> createMfrScreenConfiguration(@RequestBody MfrScreenConfigurationRequest mfrScreenConfigurationRequest) throws Exception {
        MessageModel createMFRConfiguration;



        try {
            createMFRConfiguration = mfrScreenConfigurationService.createMfrScreenConfiguration(mfrScreenConfigurationRequest);
            return ResponseEntity.ok(createMFRConfiguration);
        } catch (MFRScreenConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MFRScreenConfiguration> retrieveProduct(@RequestBody MfrScreenConfigurationRequest mfrScreenConfigurationRequest) throws JsonProcessingException {
        MFRScreenConfiguration retrieveProductResponse;
        if(mfrScreenConfigurationRequest.getSite()!=null && !mfrScreenConfigurationRequest.getSite().isEmpty()) {


            try {


                if(mfrScreenConfigurationRequest.getSite()!=null && !mfrScreenConfigurationRequest.getSite().isEmpty()) {
                    try {
                        retrieveProductResponse = mfrScreenConfigurationService.retrieveProduct(mfrScreenConfigurationRequest);
                        return ResponseEntity.ok(retrieveProductResponse);
                    }catch(MFRScreenConfigurationException e){
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }



            }catch(MFRScreenConfigurationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MFRScreenConfigurationException(9001, mfrScreenConfigurationRequest.getSite());
    }

    @PostMapping("delete")
    public ResponseEntity<com.rits.mfrscreenconfigurationservice.model.MessageModel> deleteProduct(@RequestBody MfrScreenConfigurationRequest mfrScreenConfigurationRequest) throws Exception {
        com.rits.mfrscreenconfigurationservice.model.MessageModel deleteResponse;
        if (mfrScreenConfigurationRequest.getSite() != null && !mfrScreenConfigurationRequest.getSite().isEmpty()) {

            try {

                deleteResponse = mfrScreenConfigurationService.deleteProductName(mfrScreenConfigurationRequest);




                return ResponseEntity.ok(deleteResponse);

            } catch (MFRScreenConfigurationException mfrScreenConfigurationException) {
                throw mfrScreenConfigurationException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MFRScreenConfigurationException(9001);

    }

    @PostMapping("/retrieveTop50")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseList> getProductListListByCreationDate(@RequestBody MfrScreenConfigurationRequest mfrScreenConfigurationRequest) {
        ProductResponseList top50Response;
        if(mfrScreenConfigurationRequest.getSite()!=null && !mfrScreenConfigurationRequest.getSite().isEmpty()) {
            try {
                top50Response= mfrScreenConfigurationService.getProductListListByCreationDate(mfrScreenConfigurationRequest);
                return ResponseEntity.ok(top50Response);
            }catch(MFRScreenConfigurationException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new MFRScreenConfigurationException(9001, mfrScreenConfigurationRequest.getSite());
    }
    
}
