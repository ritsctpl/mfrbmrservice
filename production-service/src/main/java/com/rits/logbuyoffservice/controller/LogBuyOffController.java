package com.rits.logbuyoffservice.controller;

import com.rits.dccollect.dto.DcCollectRequest;
import com.rits.dccollect.dto.DcGroupList;
import com.rits.dccollect.exception.DcCollectException;
import com.rits.logbuyoffservice.dto.AttachmentDetailsRequest;
import com.rits.logbuyoffservice.dto.LogbuyOffRequest;
import com.rits.logbuyoffservice.dto.LogbuyOffRequestList;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.logbuyoffservice.model.BuyoffLog;
import com.rits.logbuyoffservice.model.LogBuyOffMessageModel;
import com.rits.logbuyoffservice.service.LogBuyOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/logbuyoff-service")
public class LogBuyOffController {
    private final LogBuyOffService logBuyOffService;

    @PostMapping("accept")
    @ResponseStatus(HttpStatus.OK)
    public LogBuyOffMessageModel approve(@RequestBody LogbuyOffRequestList logbuyOffRequestList)
    {
        try {
            return logBuyOffService.accept(logbuyOffRequestList);
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("reject")
    @ResponseStatus(HttpStatus.OK)
    public LogBuyOffMessageModel reject(@RequestBody LogbuyOffRequestList logbuyOffRequestList)
    {
        try {
            return logBuyOffService.reject(logbuyOffRequestList);
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("partial")
    @ResponseStatus(HttpStatus.OK)
    public LogBuyOffMessageModel partial(@RequestBody LogbuyOffRequest  logbuyOffRequest)
    {
        try {
            return logBuyOffService.partial(logbuyOffRequest);
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("skip")
    @ResponseStatus(HttpStatus.OK)
    public LogBuyOffMessageModel skip(@RequestBody LogbuyOffRequest  logbuyOffRequest)
    {
        try {
            return logBuyOffService.skip(logbuyOffRequest);
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("getListOfBuyoff")
    @ResponseStatus(HttpStatus.OK)
    public List<BuyoffLog> getListOfBuyoff(@RequestBody AttachmentDetailsRequest attachmentDetailsRequest)
    {
        try {
            return logBuyOffService.getListOfBuyoff(attachmentDetailsRequest);
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieve")
    @ResponseStatus(HttpStatus.OK)
    public List<BuyoffLog> retrieveByBatchNo(@RequestBody LogbuyOffRequest logbuyOffRequest)
    {
        try {
            return logBuyOffService.retrieveByBatchNo(logbuyOffRequest.getSite(),logbuyOffRequest.getBatchNo());
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveByBatchAndOrderNumberAndOperation")
    @ResponseStatus(HttpStatus.OK)
    public BuyoffLog retrieveByBatchNoAndOrderNumberAndOperation(@RequestBody LogbuyOffRequest logbuyOffRequest)
    {
        try {
            return logBuyOffService.retrieveByBatchNoAndOrderNumberAndOperation(logbuyOffRequest.getSite(),logbuyOffRequest.getBatchNo(), logbuyOffRequest.getOrderNumber(), logbuyOffRequest.getOperation());
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("retrieveLoggedBuyOffList")
    @ResponseStatus(HttpStatus.OK)
    public List<BuyoffLog> retrieveLoggedBuyOffList(@RequestBody LogbuyOffRequest logbuyOffRequest)
    {
        try {
            return logBuyOffService.retrieveLoggedBuyOffList(logbuyOffRequest.getSite(),logbuyOffRequest.getPcu(), logbuyOffRequest.getBatchNo(), logbuyOffRequest.getBuyOffBO(), logbuyOffRequest.getUserId(),
                    logbuyOffRequest.getDateRange(), logbuyOffRequest.getStartDate(), logbuyOffRequest.getEndDate());
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
