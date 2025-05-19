//package com.rits.shiftservice.model;
//
//import lombok.*;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.time.LocalDateTime;
//import java.util.List;
//    @Builder
//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Document(collection = "R_SHIFT")
//    public class Shift {
//        @Id
//        private String handle;
//        private String site;
//        private String shiftName;
//        private String description;
//        private String shiftType;
//        private String workCenter;
//        private String resource;
//        private List<ShiftIntervals> shiftIntervals;
//        private List<CalendarRules> calendarRules;
//        private List<CalendarOverrides> calendarOverrides;
//        private List<CustomData> customDataList;
//        private int active;
//        private String createdBy;
//        private String modifiedBy;
//        private LocalDateTime createdDateTime;
//        private LocalDateTime modifiedDateTime;
//
//    }
//
package com.rits.shiftservice.model;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.persistence.*;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "r_shift")
public class Shift {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String site;
    private String handle;
    private String shiftId;
    private String description;
    private String shiftType;
    private String workCenterId;
    private String resourceId;
    private int shiftMeanTime;
    private int actualTime;
    private String version;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;


    @OneToMany(cascade = CascadeType.ALL )
    @JoinColumn(name = "shift_fk", referencedColumnName = "id")
    private List<ShiftIntervals> shiftIntervals;

    @OneToMany(cascade = CascadeType.ALL )
    @JoinColumn(name = "shift_fk", referencedColumnName = "id")
    private List<CalendarRules> calendarRules;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "shift_fk", referencedColumnName = "id")
    private List<CalendarOverrides> calendarOverrides;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "shift_fk", referencedColumnName = "id")
    private List<CustomData> customDataList;
    @PrePersist
    private void generateId() {
        this.id = UUID.randomUUID();  // Generate a random UUID
    }


}
