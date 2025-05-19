package com.rits.itemgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class GroupMemberListResponse {
    private String itemGroup;
    private String groupDescription;
    private String item;
}
