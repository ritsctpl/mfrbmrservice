package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionList {
    private List<MasterDataCollection> dataCollectionList;
}
