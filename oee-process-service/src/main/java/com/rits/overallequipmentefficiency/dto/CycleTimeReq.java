package com.rits.overallequipmentefficiency.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeReq {

    private String site;
    private String resourceId;
    private String workCenterId;
    private List<ItemVersionReq> itemVersionReqs; // List of item and itemVersion combinations

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemVersionReq {
        private String item;
        private String itemVersion;
    }
}
