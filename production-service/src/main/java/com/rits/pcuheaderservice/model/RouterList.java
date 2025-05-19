package com.rits.pcuheaderservice.model;

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
