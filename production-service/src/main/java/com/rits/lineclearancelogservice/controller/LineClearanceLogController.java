package com.rits.lineclearancelogservice.controller;

import com.rits.lineclearancelogservice.dto.LineClearanceLogRequest;
import com.rits.lineclearancelogservice.dto.LineClearanceLogRequestList;
import com.rits.lineclearancelogservice.model.LineClearanceLog;
import com.rits.lineclearancelogservice.model.LineClearanceLogHistoryResponse;
import com.rits.lineclearancelogservice.model.MessageResponse;
import com.rits.lineclearancelogservice.service.LineClearanceLogService;
import com.rits.lineclearanceservice.model.RetrieveLineClearanceLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/lineclearancelog-service")
public class LineClearanceLogController {
    @Autowired
    private final LineClearanceLogService lineClearanceLogService;

    @PostMapping("/start")
    public MessageResponse startLineClearanceLog(@RequestBody LineClearanceLogRequestList request) throws Exception {
        return lineClearanceLogService.startLineClearanceLog(request);
    }
    @PostMapping("/storeFile")
    public MessageResponse storeFile(@RequestBody LineClearanceLogRequestList  request){
            return lineClearanceLogService.storeFile(request);
    }

    @PostMapping("/complete")
    public MessageResponse completeLineClearanceLog(@RequestBody LineClearanceLogRequestList request) {
        return lineClearanceLogService.completeLineClearanceLog(request);
    }
    @PostMapping("/reject")
    public MessageResponse rejectLineClearanceLog(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.rejectLineClearanceLog(request);
    }

    @PostMapping("/approve")
    public MessageResponse approveLineClearanceLog(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.approveLineClearanceLog(request);
    }

    @PostMapping("/update")
    public MessageResponse updateLineClearanceLog(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.updateLineClearanceLog(request);
    }
    @PostMapping("/validate")
    public MessageResponse validateLineClearanceLog(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.validateLineClearanceLog(request);
    }
    @PostMapping("/retrieveHistory")
    public List<LineClearanceLogHistoryResponse> retriveLineClearanceLogHistory(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.retriveLineClearanceLogHistory(request);
    }

    @PostMapping("/retrieve")
    public List<RetrieveLineClearanceLogResponse> retrieveLineClearanceList(@RequestBody LineClearanceLogRequest request){
    return lineClearanceLogService.retrieveLineClearanceList(request);
    }

    @PostMapping("/retrieveLineClearanceLogList")
    public List<LineClearanceLog> retriveLineClearanceLogList(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.retriveLineClearanceLogList(request);
    }

    @PostMapping("/checkLineClearnce")
    public boolean checkLineClearance(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.checkLineClearance(request.getSite(), request.getBatchNo(), request.getResourceId(), request.getOperation(), request.getPhase());
    }

    @PostMapping("/changeLineCleranceStatusToNew")
    public boolean changeLineCleranceStatusToNew(@RequestBody LineClearanceLogRequest request){
        return lineClearanceLogService.changeLineCleranceStatusToNew(request.getSite(), request.getBatchNo(), request.getResourceId());
    }

}
