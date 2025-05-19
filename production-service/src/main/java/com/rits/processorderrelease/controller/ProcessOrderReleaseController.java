package com.rits.processorderrelease.controller;

import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderrelease.dto.ProcessOrderReleaseResponse;
import com.rits.processorderrelease.service.ProcessOrderReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/processorderrelease-service")
public class ProcessOrderReleaseController {


    @Autowired
    private ProcessOrderReleaseService processOrderReleaseService;

    @PostMapping("/release")
    public ResponseEntity<ProcessOrderReleaseResponse> releaseOrders(
            @RequestBody ProcessOrderReleaseRequest request) {
        ProcessOrderReleaseResponse response = processOrderReleaseService.releaseOrders(request);
        return ResponseEntity.ok(response);
    }


}
