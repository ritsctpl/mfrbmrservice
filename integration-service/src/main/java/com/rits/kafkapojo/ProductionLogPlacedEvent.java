package com.rits.kafkapojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionLogPlacedEvent {
    private String productionLogType;
}
