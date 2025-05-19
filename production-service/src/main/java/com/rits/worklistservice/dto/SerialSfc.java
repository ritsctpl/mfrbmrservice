package com.rits.worklistservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SerialSfc {
    private String count;
    private String serialNumber;
    private String sfcNumber;
    private String sfcQuantity;
    private String state;
    private boolean enabled;

}
