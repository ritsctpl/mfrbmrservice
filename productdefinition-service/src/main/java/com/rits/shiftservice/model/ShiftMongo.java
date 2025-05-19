package com.rits.shiftservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="R_SHIFT")
public class ShiftMongo {

        @Id
        private String handle;
        private String site;
        private String shiftId;
        private String description;
        private String shiftType;
        private String workCenterId;
        private String resourceId;
        private int active;
        private int shiftMeanTime;
        private int actualTime;
        private String version;
        private String userId;
        private String modifiedBy;
        private String createdBy;
        private LocalDateTime createdDateTime;
        private LocalDateTime modifiedDateTime;
        private List<ShiftIntervals> shiftIntervals;
        private List<CalendarRules> calendarRules;
        private List<CalendarOverrides> calendarOverrides;
        private List<CustomData> customDataList;
}
