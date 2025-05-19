package com.rits.lineclearanceservice.controller;

import com.rits.lineclearanceservice.dto.RetrieveLineClearanceLogRequest;
import com.rits.lineclearanceservice.model.LineClearance;
import com.rits.lineclearanceservice.model.LineClearanceResponse;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import com.rits.lineclearanceservice.service.LineClearanceService;
import com.rits.lineclearanceservice.model.MessageModel;
import com.rits.lineclearanceservice.dto.LineClearanceRequest;
import com.rits.lineclearanceservice.exception.LineClearanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/lineclearance-service")
public class LineClearanceController {
    private final LineClearanceService lineClearanceService;
//
    @PostMapping("create")
    public MessageModel create(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.create(request);
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.update(request);
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }
//
    @PostMapping("delete")
    public MessageModel delete(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.delete(request);
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }
//
    @PostMapping("retrieve")
    public LineClearance retrieve(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.retrieve(request);
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }
//
    @PostMapping("retrieveAll")
    public List<LineClearanceResponse> retrieveAll(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.retrieveAll(request.getSite());
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }

    @PostMapping("retrieveTop50")
    public List<LineClearanceResponse> retrieveTop50(@RequestBody LineClearanceRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.retrieveTop50(request.getSite());
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }

    @PostMapping("/isExist")
    public boolean isLineClearanceExist(@RequestBody LineClearanceRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return lineClearanceService.isLineClearanceExist(request.getSite(), request.getTemplateName());
            }catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }

    @PostMapping("retrieveLineClearanceList")
    public List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(@RequestBody RetrieveLineClearanceLogRequest request) {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return lineClearanceService.retrieveLineClearanceList(request);
            } catch (LineClearanceException lineClearanceException) {
                throw lineClearanceException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new LineClearanceException(7001);
    }
}

