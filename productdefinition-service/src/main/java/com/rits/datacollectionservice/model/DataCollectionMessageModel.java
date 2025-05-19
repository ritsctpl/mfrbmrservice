package com.rits.datacollectionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataCollectionMessageModel {
    private DataCollection response;
    private MessageDetails message_details;
    private List<DataCollection> dataCollectionList;
}
