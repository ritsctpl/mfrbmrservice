package com.rits.buyoffservice.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.buyoffservice.dto.*;
import com.rits.buyoffservice.model.BuyOff;
import com.rits.buyoffservice.model.MessageModel;

public interface BuyOffService {
    public MessageModel createBuyOff(BuyOffRequest buyOffrequest) throws Exception;

    public MessageModel update(BuyOffRequest buyOffrequest) throws Exception;

    public List<BuyOffTop50Record> retrieveTop50BuyOff(BuyOffRequest buyOffRequest) throws Exception;
    public List<BuyOffTop50Record> retrieveAll(BuyOffRequest buyOffRequest) throws Exception;

    public BuyOff retrieve(BuyOffRequest buyOffRequest) throws Exception;

    public MessageModel delete(BuyOffRequest buyOffRequest) throws Exception;

    boolean isBuyOffExist(String buyOff, String version, String site) throws Exception;

    public List<BuyOff> retrieveByAttachmentDetails(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<BuyOff> retrieveByResource(String site, String resource) throws Exception;

    public List<BuyOff> retrieveByOperation(String operation, String site) throws Exception;

    public List<BuyOff> retrieveByWorkCenter(String site, String workCenter) throws Exception;

    public List<BuyOff> retrieveByShopOrder(String site, String shopOrder) throws Exception;

    public List<BuyOff> retrieveByPcu(String site, String pcu) throws Exception;


    public List<BuyOff> retrieveByItem(String site, String item) throws Exception;

    public List<String> retrieveBuyOffNameListByItem(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameByItemAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameByPcuAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameByItemAndOperationAndRouting(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameListByResource(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameListByResourceAndPcu(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameListByShopOrderAndOperation(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameListByPcu(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameByOperationAndRoutingAndPcuAndMergeItemOp(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameByWorkCenterAndMergeResourceList(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

    public List<String> retrieveBuyOffNameListByShopOrderAndMergeItemList(AttachmentDetailsRequest attachmentDetailsRequest) throws Exception;

//    public List<String> retrieveAttachmentDetailsList(AttachmentDetailsRequest attachmentDetailsRequest);
    public BuyOff associateResourceUserGroup(AssociateUserGroup associateUserGroup) throws Exception;

    public BuyOff removeUserGroupType(AssociateUserGroup associateUserGroup) throws Exception;

    public AvailableUserGroups availableUserGroup(AssociateUserGroup associateUserGroup) throws Exception;

    public String callExtension(Extension extension);
    boolean isSkipAllowed(String handle);
    boolean isPartialAllowed(String handle);
    boolean isRejectAllowed(String handle);
}
