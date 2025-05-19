//package com.rits.shiftservice.model;
//
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Builder
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class ShiftIntervals {
//    private String startTime;
//    private String endTime;
//    private int shiftMeanTime;
//    private int actualTime;
//    private LocalDateTime validFrom;
//    private LocalDateTime validEnd;
//    private List<Break> breakList;
//}
//
package com.rits.availability.dto;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ShiftIntervals {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String handle;
    private String site;
    private String shiftRef;
    private LocalTime startTime;
    private LocalTime endTime;
    private int shiftMeanTime;
    private int actualTime;
    private LocalDateTime validFrom;
    private LocalDateTime validEnd;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "shift_interval_id", referencedColumnName = "id")
    private List<Break> breakList;
    @PrePersist
    private void generateId() {
        this.id = UUID.randomUUID();  // Generate a random UUID
    }


}
