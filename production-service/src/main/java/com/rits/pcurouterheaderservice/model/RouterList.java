package com.rits.pcurouterheaderservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RouterList {
    private String pcuRouterBO;
    private String status;
}
