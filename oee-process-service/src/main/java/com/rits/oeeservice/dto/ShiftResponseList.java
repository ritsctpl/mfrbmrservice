package com.rits.oeeservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponseList {
    private List<ShiftResponse> shiftResponseList;
}
