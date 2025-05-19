package com.rits.oeeservice.model;


import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "oee_r_shift_interval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OeeShiftInterval implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String handle;
    @Column(nullable = false)
    private String site;
    @ManyToOne
    @JoinColumn(name = "shift_id", referencedColumnName = "shiftId")
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

