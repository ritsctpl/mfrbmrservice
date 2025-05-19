package com.rits.pcustepstatus.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SerialPcu {
    private String count;
    private String serialNumber;
    private String pcuNumber;
    private String pcuQuantity;
    private String state;
    private boolean enabled;

}

