package com.rits.toolnumberservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToolNumberResponse {
    private String toolNumber;

    private String description;
    private String status;
    private String toolGroup;

    private String location;
    private String qtyAvailable;
    private String erpEquipmentNumber;
    private String erpPlanMaintenanceOrder;
    private String toolQty;
}
