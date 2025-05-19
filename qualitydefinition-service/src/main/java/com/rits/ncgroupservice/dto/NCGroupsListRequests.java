package com.rits.ncgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NCGroupsListRequests {
    private String site;
    private String ncCode;
    private List<String> ncGroup;
}
