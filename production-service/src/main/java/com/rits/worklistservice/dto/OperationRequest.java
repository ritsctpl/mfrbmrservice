package com.rits.worklistservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OperationRequest {
    private String  site;
    private String operation;
    private String version;



}
