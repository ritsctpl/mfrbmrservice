package com.rits.oeeservice.model;

import lombok.*;
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
public class Oee {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String site;
        private String shiftId;
        private String pcuId;
        private String workcenterId;
        private String category;
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
        private String operationBo;
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
        private double targetQuantity;
}



