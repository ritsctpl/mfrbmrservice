package com.rits.oeeservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ItemDetails {
    private String itemBo;
    private double actual;
    private double plan;
    private double rejection;
    private Integer productionTime;
    private Integer actualTime;
    private double downtime;
    private double goodQualityCount;
    private double badQualityCount;
}