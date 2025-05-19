package com.rits.routingservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String bom;
    private String revision;
}
