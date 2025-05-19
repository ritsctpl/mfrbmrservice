package com.rits.workinstructionservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuRouterHeader {
    private String site;
    private String handle;
    private String pcuBo;
    private String pcuRouterBo;
    private List<Router> router;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
