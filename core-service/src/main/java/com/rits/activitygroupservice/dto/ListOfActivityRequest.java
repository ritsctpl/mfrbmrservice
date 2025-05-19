package com.rits.activitygroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListOfActivityRequest {
    private String site;
    private List<String> activityId;
    private List<String> activityGroup;
//    private List<String> activityId;
}
