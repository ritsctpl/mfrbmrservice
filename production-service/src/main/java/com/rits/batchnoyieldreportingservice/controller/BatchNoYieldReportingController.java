package com.rits.batchnoyieldreportingservice.controller;

import com.rits.batchnoyieldreportingservice.dto.BatchNoYieldReportingRequest;
import com.rits.batchnoyieldreportingservice.exception.BatchNoYieldReportingException;
import com.rits.batchnoyieldreportingservice.model.BatchNoYieldReporting;
import com.rits.batchnoyieldreportingservice.model.MessageModel;
import com.rits.batchnoyieldreportingservice.service.BatchNoYieldReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/batchnoyieldreporting-service")
public class BatchNoYieldReportingController {
    private final BatchNoYieldReportingService batchNoYieldReportingService;

    @PostMapping("create")
    public MessageModel create(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.create(request);
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.update(request);
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.delete(request);
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("retrieve")
    public BatchNoYieldReporting retrieve(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.retrieve(request);
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("retrieveAll")
    public List<BatchNoYieldReporting> retrieveAll(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.retrieveAll(request.getSite());
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("retrieveTop50")
    public List<BatchNoYieldReporting> retrieveTop50(@RequestBody BatchNoYieldReportingRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoYieldReportingService.retrieveTop50(request.getSite());
            } catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }

    @PostMapping("/isExist")
    public boolean isBatchNoYieldReportingExist(@RequestBody BatchNoYieldReportingRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return batchNoYieldReportingService.isBatchNoYieldReportingExist(request.getSite(), request.getBatchNo());
            }catch (BatchNoYieldReportingException batchNoYieldReportingException) {
                throw batchNoYieldReportingException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoYieldReportingException(7001);
    }
}
