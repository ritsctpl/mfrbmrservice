package com.rits.overallequipmentefficiency.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "R_QUALITY")
public class QualityModel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String site;
        private String workcenterId;
        private String resourceId;
        private String reason;
        private String shiftId;
        private String pcu;
        private String operation;
        private String operationVersion;
        private String item;
        private String itemVersion;
        private String shopOrder;
        private Double goodQuantity;
        private Double badQuantity;
        private Double plan;
        private Double totalQuantity;
        private Double qualityPercentage;
        private String batchNumber;
        private String batchSize;
        private LocalDateTime calculationTimestamp;
        private LocalDateTime createdDateTime;
        private LocalDateTime updatedDateTime;
        private LocalDateTime intervalStartDateTime;
        private LocalDateTime intervalEndDateTime;
        private Long availabilityId;
        private Long performanceId;
        private Integer active;
        private String user_id;
        private String eventTypeOfPerformance;
        private String category;
        private double targetQuantity;

}
