package com.rits.customhookservice.controller;

import com.rits.customhookservice.exception.CustomHookException;
import com.rits.customhookservice.service.CustomHookService;
import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderstateservice.dto.ProcessOrderCompleteRequest;
import com.rits.processorderstateservice.dto.ProcessOrderStartRequest;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/customhook-service")
public class CustomHookController {
    @Autowired
    private CustomHookService customHookService;
    @PostMapping("/checkLineclearance")
    public void checkLineclearance(@RequestBody ProcessOrderStartRequest request) {
        try{
            customHookService.checkLineclearance(request);
        }catch (ProcessOrderStateException e){
            throw e;
        }
    }

    @PostMapping("/checkBuyoff")
    public void checkBuyoff(@RequestBody ProcessOrderStartRequest request) {
        try{
            customHookService.checkBuyoff(request);
        }catch (ProcessOrderStateException e){
            throw e;
        }
    }

    @PostMapping("/checkBatchInWork")
    public void checkBatchInWork(@RequestBody ProcessOrderStartRequest request) {
        try{
            customHookService.checkBatchInWork(request);
        }catch (ProcessOrderStateException e){
            throw e;
        }
    }

    @PostMapping("/orderRelease")
    public void orderRelease(@RequestBody ProcessOrderReleaseRequest request) {
        try{
            customHookService.orderRelease(request);
        }catch (CustomHookException e){
            throw e;
        }
    }

    @PostMapping("/checkTolerance")
    public void toleranceCheck(@RequestBody ProcessOrderCompleteRequest request) throws Exception {
        try{
            customHookService.checkTolerance(request);
        }catch (Exception e){
            throw e;
        }
    }
}
