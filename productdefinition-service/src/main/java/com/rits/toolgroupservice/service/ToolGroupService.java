package com.rits.toolgroupservice.service;

import com.rits.toolgroupservice.dto.AttachmentListResponseList;
import com.rits.toolgroupservice.dto.Response;
import com.rits.toolgroupservice.dto.ToolGroupListResponseList;
import com.rits.toolgroupservice.dto.ToolGroupRequest;
import com.rits.toolgroupservice.model.Attachment;
import com.rits.toolgroupservice.model.ToolGroup;
import com.rits.toolgroupservice.model.ToolGroupMessageModel;

import java.util.List;

public interface ToolGroupService {
    public ToolGroupMessageModel createToolGroup(ToolGroupRequest toolGroupRequest) throws Exception;

    public ToolGroupMessageModel updateToolGroup(ToolGroupRequest toolGroupRequest) throws Exception;

    public ToolGroupListResponseList getToolGroupListByCreationDate(ToolGroupRequest toolGroupRequest) throws Exception;

    public ToolGroupListResponseList getToolGroupList(ToolGroupRequest toolGroupRequest) throws Exception;

    public ToolGroup retrieveToolGroup(ToolGroupRequest toolGroupRequest) throws Exception;

    public ToolGroupMessageModel deleteToolGroup(ToolGroupRequest toolGroupRequest) throws Exception;

   // List<ToolGroup> retrieveAllToolGroups(ToolGroupRequest toolGroupRequest);

    public boolean isToolGroupExist(ToolGroupRequest toolGroupRequest) throws Exception;
    public AttachmentListResponseList getAttachmentList(ToolGroupRequest toolGroupRequest) throws Exception;

    List<String> generateCombinations(List<String> elements);

    void generateCombinationsHelper(List<String> elements, String prefix, int startIndex, List<String> result);

    List<ToolGroup> retrieveByAttachment(List<Attachment> attachmentList) throws Exception;
}
