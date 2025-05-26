package com.rits.workflowstatesmasterservice.controller;

import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesMasterRequest;
import com.rits.workflowstatesmasterservice.exception.WorkFlowStatesMasterException;
import com.rits.workflowstatesmasterservice.model.WorkFlowStatesMaster;
import com.rits.workflowstatesmasterservice.service.WorkFlowStatesMasterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/workflowstatesmaster-service")
public class WorkFlowStatesMasterController {
private final WorkFlowStatesMasterServiceImpl workFlowStatesMasterService;

    @PostMapping("/createWorkFlowStatesMaster")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createWorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public ResponseEntity<?> updateWorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public WorkFlowStatesMaster getWorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public List<WorkFlowStatesMaster> getAllWorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public List<WorkFlowStatesMaster> getTop50WorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public List<WorkFlowStatesMaster> getAllIsEnd(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
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
    public ResponseEntity<?> deleteWorkFlowStatesMaster(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        try {
            return ResponseEntity.ok(workFlowStatesMasterService.deleteWorkFlow(workFlowStatesMasterRequest));
        } catch (WorkFlowStatesMasterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
