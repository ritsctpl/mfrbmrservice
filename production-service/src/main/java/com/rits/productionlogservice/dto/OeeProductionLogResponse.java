package com.rits.productionlogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*@Data
@NoArgsConstructor
@AllArgsConstructor
public class OeeProductionLogResponse {
    private Double grandTotalQty;
    private List<ShopOrderBreakdown> shopOrderBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopOrderBreakdown {
        private String shopOrder;
        private Double totalQty;
    }
}*/
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
public class OeeProductionLogResponse {
    private String shiftId;
    private double grandTotalQty;
    private List<ShopOrderBreakdown> shopOrderBreakdowns;
    private String eventType;
    private String reasonCode;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopOrderBreakdown {
        private String shopOrder;
        private double totalQty;
private String batchNumber;
        private String operation;
        private String operationVersion;
        // New fields for material
        private String itemId;
        private String itemVersion;
        private String batchSize;
    }
}

