
package com.rits.performance.dto;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
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
    private String resourceType;
}

