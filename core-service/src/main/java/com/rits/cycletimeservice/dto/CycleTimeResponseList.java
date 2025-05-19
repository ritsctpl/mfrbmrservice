package com.rits.cycletimeservice.dto;

import com.rits.cycletimeservice.model.CycleTime;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeResponseList {
    private List<CycleTimeResponse> cycleTimeList;
    private List<CycleTimeRes> cycleTimeSpecificRec;
    private List<ItemAndVersionGroup> itemAndVersionGroups;
}
