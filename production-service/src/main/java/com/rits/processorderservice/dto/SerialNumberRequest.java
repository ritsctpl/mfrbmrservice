package com.rits.processorderservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SerialNumberRequest {
    private String site;
    //private String processOrder;
    private String orderNumber;
    private List<String> bnoList;
}
