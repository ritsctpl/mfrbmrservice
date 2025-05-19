package com.rits.batchnodoneservice.controller;

import com.rits.batchnodoneservice.dto.BatchNoDoneRequest;
import com.rits.batchnodoneservice.exception.BatchNoDoneException;
import com.rits.batchnodoneservice.model.BatchNoDone;
import com.rits.batchnodoneservice.model.MessageModel;
import com.rits.batchnodoneservice.service.BatchNoDoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/batchnodone-service")
public class BatchNoDoneController {
    private final BatchNoDoneService batchNoDoneService;

    @PostMapping("create")
    public MessageModel create(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.create(request);
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("update")
    public MessageModel update(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.update(request);
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("delete")
    public MessageModel delete(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.delete(request);
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("retrieve")
    public BatchNoDone retrieve(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.retrieve(request);
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("retrieveAll")
    public List<BatchNoDone> retrieveAll(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.retrieveAll(request.getSite());
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("retrieveTop50")
    public List<BatchNoDone> retrieveTop50(@RequestBody BatchNoDoneRequest request) throws Exception {
        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try {
                return batchNoDoneService.retrieveTop50(request.getSite());
            } catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }

    @PostMapping("/isExist")
    public boolean isBatchNoDoneExist(@RequestBody BatchNoDoneRequest request) throws Exception {

        if(request.getSite() != null && !request.getSite().isEmpty()) {
            try{
                return batchNoDoneService.isBatchNoDoneExist(request.getSite(), request.getBatchNo());
            }catch (BatchNoDoneException batchNoDoneException) {
                throw batchNoDoneException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new BatchNoDoneException(7001);
    }
}
