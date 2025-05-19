package com.rits.startservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcuRouterHeaderRequest {
        private String site;
        private String pcuBo;
        private String router;
        private String version;
        private String operation;
}
