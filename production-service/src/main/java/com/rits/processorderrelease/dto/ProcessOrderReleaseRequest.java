package com.rits.processorderrelease.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProcessOrderReleaseRequest {
    private List<OrderDetails> orders;
/*
c.	Site
d.	User
e.	Material

*/
    @Data
    public static class OrderDetails {
        private String processOrder;
        private BigDecimal qtyToRelease;
        private String site;
        private String user;
        private String plannedMaterial;
        private String materialVersion;
        private String materialDescription;
        private String batchNumber;
        private String orderType;
    }
}
