package com.rits.licencevalidationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NCGroupsListRequest {
    private String site;
    private String ncCode;
    private List<String> ncGroup;
}
