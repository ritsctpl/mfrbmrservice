package com.rits.datacollectionservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionResponseList {
    private List<DataCollectionResponse> dataCollectionList;
}
