package com.rits.processlotservice.dto;

import com.rits.processlotservice.model.PcuBo;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProcessLotResponse {
    private List<PcuBo> processLotMembers;
}
