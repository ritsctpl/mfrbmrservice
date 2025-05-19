package com.rits.pcuheaderservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class BomList {
    private String pcuBomBO;
    private String status;
}
