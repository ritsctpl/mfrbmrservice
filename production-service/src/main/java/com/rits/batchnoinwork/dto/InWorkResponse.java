package com.rits.batchnoinwork.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InWorkResponse {
    private String batchNo;
    private String item;
    private String itemVersion;
    private String recipe;
    private String recipeVersion;
    private BigDecimal quantity;
    private String processOrder;
    private String status;
}
