package com.rits.pcuheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuHeaderResponse {
    private String pcu;
    private String shopOrder;
    private String item;
    private String itemVersion;
}
