package com.rits.workinstructionservice.service;

import com.rits.workinstructionservice.dto.*;
import com.rits.workinstructionservice.model.Attachment;
import com.rits.workinstructionservice.model.WIMessageModel;
import com.rits.workinstructionservice.model.WorkInstruction;

import java.util.List;

public interface WorkInstructionService {
    public WIMessageModel createWorkInstruction(WorkInstructionRequest workInstructionRequest) throws  Exception;

   public  WorkInstruction uploadFile(WorkInstructionRequest workInstructionRequest) throws Exception;

    public String getFileContent(WorkInstructionRequest workInstructionRequest) throws  Exception;

    public  Boolean isWorkInstructionExists(WorkInstructionRequest workInstructionRequest) throws Exception;

    boolean workInstructionExistsByRevision(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WIMessageModel updateWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WIMessageModel  deleteWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WorkInstructionListResponse retrieveTop50(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WorkInstructionListResponse retrieveAllByWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WorkInstruction retrieveByWorkInstruction(WorkInstructionRequest workInstructionRequest) throws Exception;

    public WorkInstructionResponseList retrieveWorkInstructionByItem(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByItemOperatrion(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByPcuOperation(AttachmentListRequest attachmentListRequest) throws Exception;
 public WorkInstructionResponseList retrieveByResourceOperation(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByItemOperationRouting(AttachmentListRequest attachmentListRequest) throws Exception;
    public WorkInstructionResponseList retrieveByResource(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByResourcePcu(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByShopOrderOperation(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByPcuAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByOperationRoutingPcuAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception;

    public WorkInstructionResponseList retrieveByShopOrderAndMergeItem(AttachmentListRequest attachmentListRequest) throws Exception;
    public WorkInstructionResponseList retrieveByWorkCenterAndMergeResource(AttachmentListRequest attachmentListRequest) throws Exception;

    WorkInstructionResponseList retrieveWorkInstructionByBom(AttachmentListRequest attachmentListRequest) throws Exception;

    WorkInstructionResponseList retrieveWorkInstructionByBomVersion(AttachmentListRequest attachmentListRequest) throws Exception;

    WorkInstructionResponseList retrieveWorkInstructionByComponent(AttachmentListRequest attachmentListRequest) throws Exception;

    WorkInstructionResponseList retrieveWorkInstructionByComponentVersion(AttachmentListRequest attachmentListRequest) throws Exception;

    List<String> generateCombinations(List<String> elements);

    void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result);

   public List<WorkInstruction> getWorkInstructionByAttachment(List<Attachment> attachmentList)throws Exception;
    public List<WorkListResponse> getWorkInstructionList(WorkListRequest workListRequest) throws Exception ;
}
