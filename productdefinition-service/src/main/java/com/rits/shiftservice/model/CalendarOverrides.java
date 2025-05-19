package com.rits.shiftservice.model;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class CalendarOverrides {
//        private String date;
//        private String productionDay;
//        private String dayClass;
//    }
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="r_calendar_overrides")
public class CalendarOverrides {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String site;
    private String handle;
    private Date date;
    private String productionDay;
    private String dayClass;
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
