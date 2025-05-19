
package com.rits.cycletimeservice.model;

import lombok.*;

import java.time.LocalDateTime;
import javax.persistence.*;
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Entity
    @Builder
    @Table(name = "r_cycle_time")
    public class CycleTimePostgres {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String site;
        private String shiftId;
        private String operation;
        private String operationVersion;
        private String material;
        private String materialVersion;
        private String resourceId;
        private String resourceType;
        private String time;
        private String item;
        private String itemVersion;
        private String workcenterId;
        private String pcu;
        private Double plannedCycleTime;
        private Double manufacturedTime;
        private Integer priority;
        private Integer attachmentCount;
        private String createdBy;
        @Column(name = "created_datetime")
        private LocalDateTime createdDatetime;
        private String modifiedBy;
        private LocalDateTime modifiedDatetime;
        private String tag;
        private int active;
        private String userId;
        private String handle;
        private Double targetQuantity;
        //private String time;

    }

