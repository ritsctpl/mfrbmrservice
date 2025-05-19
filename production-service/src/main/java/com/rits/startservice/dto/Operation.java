package com.rits.startservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Operation {
    private String operation;
    private String site;
    private String revision;

    public Operation(String operation, String site){
        this.operation = operation;
        this.site = site;
    }
}
