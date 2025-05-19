package com.rits.processorderstateservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Operation {
    private String site;
    private String operation;
    private String status;
}
