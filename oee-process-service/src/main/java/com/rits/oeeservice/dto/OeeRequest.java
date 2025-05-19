package com.rits.oeeservice.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OeeRequest {
    private String site;
    private List<String> sites;
    private List<String> workcenterId;
    private List<String> resourceId;
    private List<String> shiftId;
    private List<String> itemBo;
    private String startTime;
    private String endTime;
    private List<String> batchno;
    private List<String> pcu;
    private String eventType;
    private String eventSource;
    private Boolean save;
    private String type;
    private String duration;
    private String Workcenter;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}

