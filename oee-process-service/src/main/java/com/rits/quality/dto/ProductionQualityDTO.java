package com.rits.quality.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionQualityDTO {
    private Double good_quantity;
    private Double bad_quantity;
    private Double total_quantity;
    private Double quality_percentage;
}
