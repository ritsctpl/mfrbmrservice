package com.rits.common.dto;

import com.rits.quality.dto.Item;
import com.rits.quality.dto.Operation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OeeFilterRequest {
    private String site;
    private String resource;
    private String workcenter;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> resourceId;
    private List<String> batchNumber;
    private List<String> shiftId;
    private int eventIntervalSeconds;
    // private List<String> productionLine;
    private List<String> workcenterId;
    private List<String> shoporderId;

    private List<String> itemBo;
    private List<String> item;
    private List<Operation> operation;
    private List<String> operations;
    private List<String> reasonCode;
    private List<String> sites;
    private List<String> reason;
    private String userId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String type;
    private String eventSource;

}
