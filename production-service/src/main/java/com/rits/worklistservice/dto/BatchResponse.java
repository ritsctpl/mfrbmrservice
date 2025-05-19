package com.rits.worklistservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchResponse {
    private List<WorkListResponse> tempWorkLists;
    private List<Object> allRawData;
}
