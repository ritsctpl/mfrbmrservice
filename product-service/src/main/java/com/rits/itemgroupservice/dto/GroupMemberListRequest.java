package com.rits.itemgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GroupMemberListRequest {
    private String site;
    private String itemGroup;
    private List<String> item;
}
