package com.rits.assemblyservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder

public class RouterList {
    private String pcuRouterBO;
    private String status;
}
