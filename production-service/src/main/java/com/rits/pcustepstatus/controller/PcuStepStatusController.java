package com.rits.pcustepstatus.controller;

import com.rits.pcuheaderservice.model.PcuHeader;
import com.rits.pcustepstatus.dto.PcuStepStatusRequest;
import com.rits.pcustepstatus.model.PcuStepStatus;
import com.rits.pcustepstatus.service.PcuStepStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/pcustepstatus-service")
public class PcuStepStatusController {
    private final PcuStepStatusService pcuStepStatusService;


    @PostMapping("/retrieve")
    public ResponseEntity<List<PcuStepStatus>> retrieveByPcuShopOrderProcessLot(@RequestBody PcuStepStatusRequest pcuStepStatusRequest)
    {
        try {
            List<PcuStepStatus> retrievedRecords = pcuStepStatusService.retrieveByPcuShopOrderProcessLot(pcuStepStatusRequest);
            return ResponseEntity.ok(retrievedRecords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
