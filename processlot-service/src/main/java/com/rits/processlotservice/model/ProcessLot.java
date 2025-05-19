package com.rits.processlotservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_ROUTER")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProcessLot {
    private String site;
    @Id
    private String processLot;
    private List<PcuBo> processLotMember;
    private LocalDateTime createdDateTime;
    private String createdBy;
    private int active;
}
