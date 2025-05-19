package com.rits.oeeservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OeeResponse {
    private String resource;
    private Double oee;
    private Double availability;
    private Double performance;
    private Double quality;
    private boolean downtime;
    private ItemDetails itemDetails;

}