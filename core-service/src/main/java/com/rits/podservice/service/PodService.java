package com.rits.podservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rits.podservice.dto.*;
import com.rits.podservice.model.MessageModel;
import com.rits.podservice.model.Pod;
import org.springframework.http.ResponseEntity;

public interface PodService {
    public MessageModel createPod(PodRequest podRequest) throws Exception;

    public Pod retrievePod(PodRequest podRequest) throws Exception;

    public MessageModel updatePod(PodRequest podRequest) throws Exception;

    public MessageModel deletePod(DeleteRequest deleteRequest) throws Exception;

    public RButtonResponseList getButtonList(ButtonListRequest buttonListRequest) throws Exception;

 public PodResponseList getPodListByCreationDate(PodListRequest podListRequest) throws Exception;

   public PodResponseList getPodList(PodListRequest podListRequest) throws Exception;

    public Boolean isPodExist(PodExistRequest podExistRequest) throws Exception;

}
