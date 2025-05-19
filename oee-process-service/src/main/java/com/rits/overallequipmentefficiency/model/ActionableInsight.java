package com.rits.overallequipmentefficiency.model;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "actionable_insights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionableInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insight_text", nullable = false, columnDefinition = "TEXT")
    private String insightText;

    @Column(name = "suggested_action")
    private String suggestedAction;

    @Column(name = "expected_gain_percent")
    private Double expectedGainPercent;

    private String site;
    private String workcenterId;
    private String resourceId;
    private String operation;
    private String item;
    private String itemVersion;
    private String shiftId;

    private LocalDate logDate;
    private String category;
    private String insightType;
    private String severity;

    private Integer impactScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active = true; // default to true for new entries
}
