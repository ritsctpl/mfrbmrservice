package com.rits.oeeservice.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
@Data
@Entity
@Table(name = "oee_r_shift")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OeeShift implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String handle;
    private String site;
    private String shiftId;
    private String description;
    private String shiftType;
    private String workcenterId;
    private String resourceId;
    private String createdBy;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL)
    private List<OeeShiftInterval> shiftIntervals;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private Integer active;

}
