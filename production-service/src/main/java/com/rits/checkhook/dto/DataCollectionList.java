package com.rits.checkhook.dto;

import com.rits.dccollect.dto.DataCollection;
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
