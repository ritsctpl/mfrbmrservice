package com.rits.overallequipmentefficiency.model;

import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "R_OEE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OeeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String site;
    private String shiftId;
    private String pcuId;
    private String workcenterId;
    private String resourceId;
    private String operation;
    private String operationVersion;
    private String routingBo;
    private String itemBo;
    private String item;
    private String itemVersion;
    private String shoporderId;
    private double totalDowntime;
    private double availability;
    private double performance;
    private double quality;
    private double goodQty;
    private double badQty;
    private double totalQty;
    private double oee;
    private int plan;
    private double productionTime;
    private double actualTime;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDateTime;
    private LocalDateTime intervalStartDateTime;
    private LocalDateTime intervalEndDateTime;
    private Long availabilityId;
    private Long performanceId;
    private Long qualityId;
    private int active;
    private String eventTypeOfPerformance;
    private String batchNumber;
    private String batchSize;
    private String reason;
    private String rootCause;
    private String category;
    private double targetQuantity;

    // New field to store the event source explicitly (optional to set directly)
    private String eventSource;

    // Derive the event source if not explicitly set.
    public String getEventSource() {
        // If the eventSource field is already explicitly set, use it.
        if (StringUtils.hasText(eventSource)) {
            return eventSource;
        }

        // If eventTypeOfPerformance is missing, return a default (manual).
        if (!StringUtils.hasText(eventTypeOfPerformance)) {
            return "MANUAL";
        }

        // Convert event type to lowercase for easier comparison.
        String eventTypeLower = eventTypeOfPerformance.toLowerCase();

        // For RESOURCE category:
        if ("RESOURCE".equalsIgnoreCase(category)) {
            if (eventTypeLower.startsWith("machine")) {
                if (eventTypeLower.contains("complete")) {
                    return "MACHINE_COMPLETE";
                } else if (eventTypeLower.contains("done")) {
                    return "MACHINE_DONE";
                }
            } else {
                if (eventTypeLower.contains("complete")) {
                    return "MANUAL_COMPLETE";
                } else if (eventTypeLower.contains("done")) {
                    return "MANUAL_DONE";
                }
            }
        }
        // For WORKCENTER category:
        else if ("WORKCENTER".equalsIgnoreCase(category)) {
            // To be safe, verify that the event type contains "done"
            if (!eventTypeLower.contains("done")) {
                // Log a warning if needed and default to a manual done type.
                // For example: logger.warn("Unexpected workcenter eventType: {} does not contain 'done'", eventTypeOfPerformance);
                return "MANUAL_DONE";
            }
            // Now return based on whether it's a machine event.
            if (eventTypeLower.startsWith("machine")) {
                return "MACHINE_DONE";
            } else {
                return "MANUAL_DONE";
            }
        }

        // If the category doesn't match RESOURCE or WORKCENTER, default to manual.
        return "MANUAL";
    }



}
