package com.rits.oeeservice.dto;
//
//import lombok.*;
//
//@Builder
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class CalendarRules {
//    private String day;
//    private String productionDay;
//    private String dayClass;
//}

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="r_calendar_rules")
public class CalendarRules {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String site;
    private String handle;
    private String day;
    private String productionDay;
    private String dayClass;
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
