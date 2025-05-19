package com.rits.processorderstateservice.controller;

import com.rits.lineclearanceservice.exception.LineClearanceException;
import com.rits.logbuyoffservice.exception.LogBuyOffException;
import com.rits.processorderstateservice.dto.*;
import com.rits.processorderstateservice.exception.ProcessOrderStateException;
import com.rits.processorderstateservice.service.ProcessOrderStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/processorderstate-service")
public class ProcessOrderStateController {

    @Autowired
    private ProcessOrderStateService processOrderStartService;

    @PostMapping("/start")
    public ResponseEntity<ProcessOrderStartResponse> startProcess(@RequestBody ProcessOrderStartRequest request) {
        ProcessOrderStartResponse response;

        try {
            response = processOrderStartService.startProcess(request);
            return ResponseEntity.ok(response);
        } catch (ProcessOrderStateException e) {
            throw e;
        } catch (LogBuyOffException e) {
            throw e;
        } catch (LineClearanceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/signoff")
    public ResponseEntity<ProcessOrderSignoffResponse> signoffProcess(@RequestBody ProcessOrderSignoffRequest request) {
        ProcessOrderSignoffResponse response;

        try {
            response = processOrderStartService.signoffProcess(request);
            return ResponseEntity.ok(response);
        } catch (ProcessOrderStateException e) {
            throw e;
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<ProcessOrderCompleteResponse> processOrderComplete(@RequestBody ProcessOrderCompleteRequest request) {
        try {
            ProcessOrderCompleteResponse response = processOrderStartService.processOrderComplete(request);
            return ResponseEntity.ok(response);
        }catch(ProcessOrderStateException e){
            throw e;
        } catch (LogBuyOffException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/testStart")
    public String testStart(@RequestBody ProcessOrderStartRequest request) throws Exception {
        // Call the new method in ProcessOrderStateServiceImpl
        return processOrderStartService.testHookableProcessStart(request);
    }
}
