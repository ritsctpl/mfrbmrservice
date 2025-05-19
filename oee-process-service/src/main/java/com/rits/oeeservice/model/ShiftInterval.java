package com.rits.oeeservice.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "r_shift_interval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ShiftInterval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String handle;
    @Column(nullable = false)
    private String site;
    @ManyToOne
    @JoinColumn(name = "shift_id", referencedColumnName = "id")
    private OeeShift shift;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer shiftMeanTime;
    private Integer actualTime;
    private LocalDateTime validFrom;
    private LocalDateTime validEnd;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private Integer active;
}
