package com.rits.workflowstatesmasterservice.service;

import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesMasterRequest;
import com.rits.workflowstatesmasterservice.model.MessageModel;
import com.rits.workflowstatesmasterservice.model.WorkFlowStatesMaster;

import java.util.List;

public interface WorkFlowStatesMasterService {
    MessageModel createWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    MessageModel updateWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    WorkFlowStatesMaster retrieveWorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    List<WorkFlowStatesMaster> retrieveAllWorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    List<WorkFlowStatesMaster> retrieveTop50WorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    List<WorkFlowStatesMaster> retrieveWorkFlowStatesByIsEnd(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;

    MessageModel deleteWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest)throws Exception;
}
