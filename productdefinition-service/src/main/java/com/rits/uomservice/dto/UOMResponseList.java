package com.rits.uomservice.dto;

import com.rits.workcenterservice.dto.WorkCenterResponse;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UOMResponseList {
    private List<UOMResponse> uomList;
}
