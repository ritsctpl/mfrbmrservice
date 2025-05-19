package com.rits.batchnophaseprogressservice.controller;

import com.rits.batchnophaseprogressservice.dto.BatchNoPhaseProgressRequest;
import com.rits.batchnophaseprogressservice.model.BatchNoPhaseProgress;
import com.rits.batchnophaseprogressservice.exception.BatchNoPhaseProgressException;
import com.rits.batchnophaseprogressservice.model.MessageModel;
import com.rits.batchnophaseprogressservice.service.BatchNoPhaseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/batchnophaseprogress-service")
public class BatchNoPhaseProgressController {
    private final BatchNoPhaseProgressService batchNoPhaseProgressService;

    @PostMapping("create")
    public MessageModel create(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.create(request);
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.update(request);
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.delete(request);
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("retrieve")
    public BatchNoPhaseProgress retrieve(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.retrieve(request);
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("retrieveAll")
    public List<BatchNoPhaseProgress> retrieveAll(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.retrieveAll(request.getSite());
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("retrieveTop50")
    public List<BatchNoPhaseProgress> retrieveTop50(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoPhaseProgressService.retrieveTop50(request.getSite());
            } catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }

    @PostMapping("/isExist")
    public boolean isBatchNoPhaseProgressExist(@RequestBody BatchNoPhaseProgressRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return batchNoPhaseProgressService.isBatchNoPhaseProgressExist(request.getSite(), request.getBatchNo());
            }catch (BatchNoPhaseProgressException batchNoPhaseProgressException) {
                throw batchNoPhaseProgressException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoPhaseProgressException(7001);
    }
}
