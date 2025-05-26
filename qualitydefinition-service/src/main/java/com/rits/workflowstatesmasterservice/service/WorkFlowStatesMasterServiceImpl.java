package com.rits.workflowstatesmasterservice.service;

import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesMasterRequest;
import com.rits.workflowstatesmasterservice.dto.WorkFlowStatesResponse;
import com.rits.workflowstatesmasterservice.model.MessageDetails;
import com.rits.workflowstatesmasterservice.model.MessageModel;
import com.rits.workflowstatesmasterservice.model.WorkFlowStatesMaster;
import com.rits.workflowstatesmasterservice.repository.WorkFlowStatesMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkFlowStatesMasterServiceImpl implements WorkFlowStatesMasterService {

    private final WorkFlowStatesMasterRepository workFlowStatesMasterRepository;
    public WorkFlowStatesMaster createOrUpdateBuilder(WorkFlowStatesMaster workFlowStatesMaster, WorkFlowStatesMasterRequest workFlowStatesMasterRequest, Boolean isRecordExist)
    {
     if(isRecordExist)
     {
        if(workFlowStatesMaster.getDescription() == null & workFlowStatesMasterRequest.getDescription() == null)
        {
            workFlowStatesMasterRequest.setDescription(workFlowStatesMaster.getName());
        }
        workFlowStatesMaster.setDescription(workFlowStatesMasterRequest.getDescription());
        workFlowStatesMaster.setAppliesTo(workFlowStatesMasterRequest.getAppliesTo());
        workFlowStatesMaster.setEditableFields(workFlowStatesMasterRequest.getEditableFields());
        workFlowStatesMaster.setIsEnd(workFlowStatesMasterRequest.getIsEnd());
        workFlowStatesMaster.setEntityType(workFlowStatesMasterRequest.getEntityType());
        workFlowStatesMaster.setModifiedBy(workFlowStatesMasterRequest.getUserId());
        workFlowStatesMaster.setModifiedDateTime(LocalDateTime.now());
        workFlowStatesMaster.setIsActive(workFlowStatesMasterRequest.getIsActive());
     }else{
         if(workFlowStatesMasterRequest.getDescription() == null)
         {
             workFlowStatesMasterRequest.setDescription(workFlowStatesMaster.getName());
         }
         workFlowStatesMaster = WorkFlowStatesMaster.builder()
                 .handle("WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName())
                 .site(workFlowStatesMasterRequest.getSite())
                 .name(workFlowStatesMasterRequest.getName())
                 .description(workFlowStatesMasterRequest.getDescription())
                 .appliesTo(workFlowStatesMasterRequest.getAppliesTo())
                 .editableFields(workFlowStatesMasterRequest.getEditableFields())
                 .isEnd(workFlowStatesMasterRequest.getIsEnd())
                 .isActive(workFlowStatesMasterRequest.getIsActive())
                 .entityType(workFlowStatesMasterRequest.getEntityType())
                 .createdBy(workFlowStatesMasterRequest.getUserId())
                 .createdDateTime(LocalDateTime.now())
                 .active(1)
                 .build();
     }
        return workFlowStatesMaster;
    }
    @Override
    public MessageModel createWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception
    {
        Boolean   isExist =  workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        if(isExist)
        {
          throw new Exception(workFlowStatesMasterRequest.getName() +" already exist");
        }
        WorkFlowStatesMaster workFlowStatesMaster = createOrUpdateBuilder(null, workFlowStatesMasterRequest, false);
        return MessageModel.builder().message_details(new MessageDetails(workFlowStatesMasterRequest.getName() + " Created SuccessFully", "S")).response(workFlowStatesMasterRepository.save(workFlowStatesMaster)).build();
    }

    @Override
    public MessageModel updateWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception
    {
        Boolean   isExist =  workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        if(!isExist)
        {
          throw new Exception(workFlowStatesMasterRequest.getName() + " does not exist");
        }
        WorkFlowStatesMaster workFlowStatesMaster = workFlowStatesMasterRepository.findBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        workFlowStatesMaster = createOrUpdateBuilder(workFlowStatesMaster, workFlowStatesMasterRequest, true);
        return MessageModel.builder().message_details(new MessageDetails(workFlowStatesMasterRequest.getName() + " Updated SuccessFully", "S")).response(workFlowStatesMasterRepository.save(workFlowStatesMaster)).build();
    }

    @Override
    public WorkFlowStatesMaster retrieveWorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesRequest) throws Exception
    {
        Boolean   isExist =  workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesRequest.getSite(), "WorkFlowBO:" + workFlowStatesRequest.getSite() + "," + workFlowStatesRequest.getName(), 1);
        if(!isExist)
        {
          throw new Exception(workFlowStatesRequest.getName() + " does not exist");
        }
        return workFlowStatesMasterRepository.findBySiteAndHandleAndActiveEquals(workFlowStatesRequest.getSite(), "WorkFlowBO:" + workFlowStatesRequest.getSite() + "," + workFlowStatesRequest.getName(), 1);
    }

    @Override
    public List<WorkFlowStatesResponse> retrieveAllWorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        return workFlowStatesMasterRepository.findBySiteAndNameContainsIgnoreCaseAndActiveEquals(workFlowStatesMasterRequest.getSite(), workFlowStatesMasterRequest.getName(), 1);
    }

    @Override
    public List<WorkFlowStatesResponse> retrieveTop50WorkFlowStates(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        return workFlowStatesMasterRepository.findTop50BySiteAndActiveEqualsOrderByCreatedDateTimeDesc(workFlowStatesMasterRequest.getSite(), 1);
    }

    @Override
    public List<WorkFlowStatesResponse> retrieveWorkFlowStatesByIsEnd(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        return workFlowStatesMasterRepository.findBySiteAndIsEndAndActiveEquals(workFlowStatesMasterRequest.getSite(), workFlowStatesMasterRequest.getIsEnd(), 1);
    }

    @Override
    public MessageModel deleteWorkFlow(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        Boolean isExist = workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        if (!isExist) {
            throw new Exception(workFlowStatesMasterRequest.getName() + " does not exist");
        }
        WorkFlowStatesMaster workFlowStatesMaster = workFlowStatesMasterRepository.findBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        workFlowStatesMaster.setActive(0);
        return MessageModel.builder().message_details(new MessageDetails(workFlowStatesMasterRequest.getName() + " Deleted SuccessFully", "S")).response(workFlowStatesMasterRepository.save(workFlowStatesMaster)).build();
    }

    @Override
    public MessageModel deactivateRecord(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        Boolean isExist = workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        if (!isExist) {
            throw new Exception(workFlowStatesMasterRequest.getName() + " does not exist");
        }
        WorkFlowStatesMaster workFlowStatesMaster = workFlowStatesMasterRepository.findBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        workFlowStatesMaster.setIsActive(false);
        return MessageModel.builder().message_details(new MessageDetails(workFlowStatesMasterRequest.getName() + " Deactivated SuccessFully", "S")).response(workFlowStatesMasterRepository.save(workFlowStatesMaster)).build();
    }

    @Override
    public MessageModel reactivateRecord(WorkFlowStatesMasterRequest workFlowStatesMasterRequest) throws Exception {
        Boolean isExist = workFlowStatesMasterRepository.existsBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        if (!isExist) {
            throw new Exception(workFlowStatesMasterRequest.getName() + " does not exist");
        }
        WorkFlowStatesMaster workFlowStatesMaster = workFlowStatesMasterRepository.findBySiteAndHandleAndActiveEquals(workFlowStatesMasterRequest.getSite(), "WorkFlowBO:" + workFlowStatesMasterRequest.getSite() + "," + workFlowStatesMasterRequest.getName(), 1);
        workFlowStatesMaster.setIsActive(true);
        return MessageModel.builder().message_details(new MessageDetails(workFlowStatesMasterRequest.getName() + " Reactivated SuccessFully", "S")).response(workFlowStatesMasterRepository.save(workFlowStatesMaster)).build();
    }
}
