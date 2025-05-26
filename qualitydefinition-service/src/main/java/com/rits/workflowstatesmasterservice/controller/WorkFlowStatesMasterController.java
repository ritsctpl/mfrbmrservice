package com.rits.workflowstatesmasterservice.controller;

import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesMasterRequest;
import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesResponse;
import com.rits.workflowstatesmasterservice.exception.WorkFlowStatesMasterException;
import com.rits.workflowstatesmasterservice.model.WorkFlowStatesMaster;
import com.rits.workflowstatesmasterservice.service.WorkFlowStatesMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/workflowstatesmaster-service")
public class WorkFlowStatesMasterController {
private final WorkFlowStatesMasterServiceImpl workFlowStatesMasterService;

    @PostMapping("/createWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createWorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return ResponseEntity.ok(workFlowStatesMasterService.createWorkFlow(workFlowStatesMasterRequest));
        }catch (WorkFlowStatesMasterException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/updateWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateWorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return ResponseEntity.ok(workFlowStatesMasterService.updateWorkFlow(workFlowStatesMasterRequest));
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.OK)
    public WorkFlowStatesMaster getWorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return workFlowStatesMasterService.retrieveWorkFlowStates(workFlowStatesMasterRequest);
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAllWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.OK)
    public List<WorkFlowStatesResponse> getAllWorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return workFlowStatesMasterService.retrieveAllWorkFlowStates(workFlowStatesMasterRequest);
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getTop50WorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.OK)
    public List<WorkFlowStatesResponse> getTop50WorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return workFlowStatesMasterService.retrieveTop50WorkFlowStates(workFlowStatesMasterRequest);
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAllIsEnd")
    @ResponseStatus(HttpStatus.OK)
    public List<WorkFlowStatesResponse> getAllIsEnd(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return workFlowStatesMasterService.retrieveWorkFlowStatesByIsEnd(workFlowStatesMasterRequest);
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/deleteWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteWorkFlowStatesMaster(@RequestBody WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return ResponseEntity.ok(workFlowStatesMasterService.deleteWorkFlow(workFlowStatesMasterRequest));
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
