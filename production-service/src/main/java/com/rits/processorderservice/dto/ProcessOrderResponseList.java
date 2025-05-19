package com.rits.processorderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderResponseList {
    private List<ProcessOrderResponse> processOrderResponseList;
}
