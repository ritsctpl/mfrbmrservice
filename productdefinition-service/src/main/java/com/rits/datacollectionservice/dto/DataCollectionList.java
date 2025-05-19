package com.rits.datacollectionservice.dto;

import com.rits.datacollectionservice.model.DataCollection;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionList {
    private List<DataCollection> dataCollectionList;
}
