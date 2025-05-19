package com.rits.processorderrelease_old.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SerialBno {
    private String count;
    private String serialNumber;
    private String bnoNumber;
    private String bnoQuantity;
    private String state;
    private boolean enabled;

}

