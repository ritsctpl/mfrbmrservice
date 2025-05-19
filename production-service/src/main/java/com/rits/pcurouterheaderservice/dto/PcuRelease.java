package com.rits.pcurouterheaderservice.dto;

import com.rits.pcurouterheaderservice.model.PcuBo;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuRelease {
    private String site;
    private PcuBo pcuBo;
    private String routing;
    private String version;
    private String operation;
    private String revision;
    private String userBO;
}
