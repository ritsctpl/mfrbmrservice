package com.rits.datacollectionservice.dto;

import com.rits.datacollectionservice.model.Attachment;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveRequest {
    private String site;
    private String sequence;
    private String itemGroup;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String operation;
    private String workCenter;
    private String resource;
    private String shopOrder;
    private String pcu;

    private List<Attachment> attachmentList;
    Map<String,String> recipeDc;
    private String batchNo;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String phaseId;
    private String operationId;

}
