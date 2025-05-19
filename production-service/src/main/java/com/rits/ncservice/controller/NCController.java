package com.rits.ncservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rits.ncservice.dto.DispositionRoutings;
import com.rits.ncservice.dto.NCCode;
import com.rits.ncservice.dto.NCCodeRequest;
import com.rits.ncservice.exception.NCException;
import com.rits.ncservice.service.NCService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/nc-service")
public class NCController {
    private final NCService ncService;
    private final ObjectMapper objectMapper;

    //    {
    //        "site":"RITS",
    //            "ncCode":"nc1"
    //    }
    @PostMapping("/retrieveDispositionRoutings")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getNCCode(@RequestBody NCCodeRequest ncCodeRequest ) {
        List<DispositionRoutings> dispositionRoutings;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                dispositionRoutings= ncService.getDispositionRoutings(ncCodeRequest.getNcCode(),ncCodeRequest.getSite());
                return ResponseEntity.ok(dispositionRoutings);
            }catch(NCException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCException(5002, ncCodeRequest.getSite());
    }
    @PostMapping("/logNC")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> logNC(@RequestBody NCCodeRequest ncCodeRequest ) {
        List<DispositionRoutings> dispositionRoutings;
        if(ncCodeRequest.getSite()!=null && !ncCodeRequest.getSite().isEmpty()) {
            try {
                dispositionRoutings= ncService.getDispositionRoutings(ncCodeRequest.getNcCode(),ncCodeRequest.getSite());
                return ResponseEntity.ok(dispositionRoutings);
            }catch(NCException e){
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new NCException(5002, ncCodeRequest.getSite());
    }

}
