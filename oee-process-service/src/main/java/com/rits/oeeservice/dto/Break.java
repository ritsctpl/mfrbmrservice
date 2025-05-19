//package com.rits.shiftservice.model;
//
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Break {
//    private String uniqueId;
//    private String breakId;
//    private String shiftType;
//    private String breakTimeStart;
//    private String breakTimeEnd;
//    private String meanTime;
//    private String reason;
//}
package com.rits.oeeservice.dto;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "r_break")
public class Break {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String handle;
    private String site;
    private String uniqueId;
    private String breakId;
    private String shiftType;
    private String intervalRef;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    private int meanTime;
    private String reason;
    private String shiftRef;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;
    @PrePersist
    private void generateId() {
        this.id = UUID.randomUUID();  // Generate a random UUID
    }
}
