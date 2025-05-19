package com.rits.shiftservice.dto;

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
