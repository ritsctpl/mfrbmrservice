package com.rits.workcenterservice.dto;

import com.rits.workcenterservice.model.WorkCenter;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkCenterResponseList {
    private List<WorkCenterResponse> workCenterList;
}
