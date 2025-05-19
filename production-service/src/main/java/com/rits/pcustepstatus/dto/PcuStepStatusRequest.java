package com.rits.pcustepstatus.dto;

import com.rits.pcustepstatus.model.PcuStepStatusComponent;
import com.rits.pcustepstatus.model.PcuStepStatusMain;
import com.rits.pcustepstatus.model.PcuStepStatusMiscellaneous;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatusRequest {
    private String site;
    private List<String> object;
    private String routingBO;
    private String user;
    private PcuStepStatusMain main;
    private PcuStepStatusComponent component;
    private PcuStepStatusMiscellaneous miscellaneous;
}
