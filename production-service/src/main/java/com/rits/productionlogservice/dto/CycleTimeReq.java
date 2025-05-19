package com.rits.productionlogservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode  // <-- This ensures uniqueness in HashSet
public class CycleTimeReq {

    private String site;
    private String resourceId;
    private String workCenterId;
    private List<ItemVersionReq> itemVersionReqs; // List of item and itemVersion combinations

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode  // <-- This ensures uniqueness in HashSet
    public static class ItemVersionReq {
        private String item;
        private String itemVersion;
    }
}
