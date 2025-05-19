package com.rits.processorderservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchNumber {
    private String count;
    private String serialNumber;
    private String batchNumber;
    private String batchNumberQuantity;
    private String state;
    private boolean enabled;

}

