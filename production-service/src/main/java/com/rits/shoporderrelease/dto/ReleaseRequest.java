package com.rits.shoporderrelease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReleaseRequest {
    private String shopOrder;
    private String site;
    private boolean addToNewProcessLot;
    private String availableQtyToRelease;
    private String qtyToRelease;
    private String plannedMaterial;
    private String materialVersion;
    private String plannedBom;
    private String bomVersion;
    private String plannedRouting;
    private String user;
    private String routingVersion;
    private String plannedWorkCenter;
    private String plannedStart;
    private String plannedCompletion;
    private String nextNumberActivity;
    private List<Pcu> pcuBOList;
    private String itemGroup;
    private String topic;
}
