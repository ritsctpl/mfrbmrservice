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
public class AddRemoveActivityRequest {
    private List<String> activityGroupName;
    private String activityId;
}
