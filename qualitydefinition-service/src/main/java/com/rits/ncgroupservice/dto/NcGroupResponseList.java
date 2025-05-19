package com.rits.ncgroupservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcGroupResponseList {
    private List<NcGroupResponse> ncGroupList;
}
