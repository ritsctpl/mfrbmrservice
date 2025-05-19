package com.rits.pcurouterheaderservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Operation {
    private String  operation;
    private String  revision;
    private String defaultResource;
}
