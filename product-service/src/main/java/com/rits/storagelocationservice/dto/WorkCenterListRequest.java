package com.rits.storagelocationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkCenterListRequest {
    private String site;
    private String storageLocation;
    private List<String> workCenter;
}
