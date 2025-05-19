
package com.rits.productionlogservice.dto;

        import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionLogDTO {
    private String site;
    private String workcenterId;
    private String operation;
    private String operationVersion;
    private String resourceId;
    private String item;
    private String itemVersion;
    private String shiftId;
    private String pcu;

    public ProductionLogDTO(String site, String workcenterId, String operation, String operationVersion, String resourceId, String item, String itemVersion, String shiftId, String pcu) {
        this.site = site;
        this.workcenterId = workcenterId;
        this.operation = operation;
        this.operationVersion = operationVersion;
        this.resourceId = resourceId;
        this.item = item;
        this.itemVersion = itemVersion;
        this.shiftId = shiftId;
        this.pcu = pcu;
    }

    // Add other fields if needed, e.g., `partsToBeProduced`
    private Double partsToBeProduced;
}
