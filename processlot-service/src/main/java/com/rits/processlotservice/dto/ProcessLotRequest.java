package com.rits.processlotservice.dto;

import com.rits.processlotservice.model.PcuBo;
import lombok.*;


import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProcessLotRequest {
    private String site;
    private String processLot;
    private List<PcuBo> processLotMember;
    private String createdBy;
}
