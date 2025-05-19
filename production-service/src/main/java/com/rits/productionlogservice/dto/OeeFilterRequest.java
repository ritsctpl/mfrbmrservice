package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OeeFilterRequest {
    private String site;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> batchNumber;
    private List<String> shiftId;
    private List<String> workcenterId;
    private List<String> shoporderId;
    private List<String> resourceId;
    private List<String> itemBo;
    private List<Item> item;
    private List<Operation> operation;
    private List<String> reasonCode;
    private List<String> sites;
    private String userId;

}
