package com.rits.toollogservice.controller;

import com.rits.toollogservice.dto.ToolLogRequest;
import com.rits.toollogservice.exception.ToolLogException;
import com.rits.toollogservice.model.ToolLog;
import com.rits.toollogservice.model.ToolLogMessageModel;
import com.rits.toollogservice.service.ToolLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/toollog-service")
public class ToolLogController {

    private final ToolLogService toolLogService;
    @PostMapping("/logTool")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ToolLogMessageModel> logTool(@RequestBody ToolLogRequest toolNumberRequest) {
        ToolLogMessageModel logToolResponse=null;
        try {
            logToolResponse= toolLogService.logTool(toolNumberRequest);
            return ResponseEntity.ok(logToolResponse);
        }catch(ToolLogException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/retrieveBySiteAndPcu")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ToolLog>> retrieveBySiteAndPcu(@RequestBody ToolLogRequest toolNumberRequest) {
        List<ToolLog> logToolResponse;
        try {
            logToolResponse= toolLogService.retrieveBySiteAndPcu(toolNumberRequest.getSite(),toolNumberRequest.getPcuBO());
            return ResponseEntity.ok(logToolResponse);
        }catch(ToolLogException e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
