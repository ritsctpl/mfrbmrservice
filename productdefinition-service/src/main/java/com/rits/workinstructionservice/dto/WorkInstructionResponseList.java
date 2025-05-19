package com.rits.workinstructionservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkInstructionResponseList {
    private List<WorkInstructionResponse> workInstructionResponse;
}
