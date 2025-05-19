package com.rits.uomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UOMResponse {
    private Integer id;
    private String uomCode;
    private String description;
    private Double conversionFactor;
    private String status;
    private String site;
   // private Integer active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
