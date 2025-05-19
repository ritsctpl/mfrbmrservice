package com.rits.pcustepstatus.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcuRouterHeaderRequest {
        private String site;
        private String pcuBo;
        private String pcuRouterBo;
}
