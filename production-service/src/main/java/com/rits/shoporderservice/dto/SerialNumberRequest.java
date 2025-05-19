package com.rits.shoporderservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SerialNumberRequest {
    private String site;
    private String shopOrder;
    private List<String> serialNumberList;
}
