package com.rits.pcucompleteservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.pcucompleteservice.dto.PcuCompleteReq;
import com.rits.pcucompleteservice.dto.RequestList;
import com.rits.pcucompleteservice.exception.PcuCompleteException;
import com.rits.pcucompleteservice.model.MessageModel;
import com.rits.pcucompleteservice.service.PcuCompleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/pcucomplete-service")
public class PcuCompleteController {
    private final PcuCompleteService pcuCompleteService;
    private final ObjectMapper objectMapper;

    @PostMapping("insert")
    public ResponseEntity<?> insert(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
        MessageModel insert;

        try {
            insert = pcuCompleteService.insert(pcuCompleteReq);
            return ResponseEntity.ok(MessageModel.builder().message_details(insert.getMessage_details()).response(insert.getResponse()).build());
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("update")
    public ResponseEntity<MessageModel> update(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
        MessageModel update;
        try {
            update = pcuCompleteService.update(pcuCompleteReq);
            return ResponseEntity.ok(MessageModel.builder().message_details(update.getMessage_details()).response(update.getResponse()).build());
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("delete")
    public ResponseEntity<Boolean> delete(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
       boolean delete;

        try {
            delete = pcuCompleteService.delete(pcuCompleteReq);
            return ResponseEntity.ok(delete);

        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("complete")
    public ResponseEntity<MessageModel> complete(@RequestBody RequestList requestList) throws Exception {
       MessageModel complete;

       try {
            complete = pcuCompleteService.complete(requestList);
            return ResponseEntity.ok(complete);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("inQueue")
    public ResponseEntity<MessageModel> inqueue(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
        MessageModel insertOrUpdateInPcuInQueue;
        try {

            insertOrUpdateInPcuInQueue = pcuCompleteService.insertOrUpdateInPcuInQueue(pcuCompleteReq);
            return ResponseEntity.ok(insertOrUpdateInPcuInQueue);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieve")
    public ResponseEntity<PcuCompleteReq> retrieve(@RequestBody PcuCompleteReq pcuCompleteReq) throws Exception {
        PcuCompleteReq retrieve;
        try {
            retrieve = pcuCompleteService.retrieve(pcuCompleteReq);
            return ResponseEntity.ok(retrieve);

        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByOperation")
    public List<PcuCompleteReq> retrieveByOperation(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return pcuCompleteService.retrieveByOperation(pcuCompleteReq);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("retrieveByOperationAndShopOrder")
    public List<PcuCompleteReq> retrieveByOperationAndShopOrder(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return pcuCompleteService.retrieveByOperationAndShopOrder(pcuCompleteReq);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("deleteByPcu")
    public Boolean deleteByPcu(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return pcuCompleteService.deleteByPcu(pcuCompleteReq);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("unDeleteByPcu")
    public Boolean unDeleteByPcu(@RequestBody PcuCompleteReq pcuCompleteReq)
    {
        try {
            return pcuCompleteService.unDeleteByPcu(pcuCompleteReq);
        } catch (PcuCompleteException pcuCompleteException) {
            throw pcuCompleteException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
