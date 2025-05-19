package com.rits.pcuheaderservice.dto;


import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class BomRequest {
    private String site;
    private String bom;
    private String revision;
}
