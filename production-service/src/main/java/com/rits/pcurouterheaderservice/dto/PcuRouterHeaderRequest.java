package com.rits.pcurouterheaderservice.dto;

import com.rits.pcurouterheaderservice.model.BomList;
import com.rits.pcurouterheaderservice.model.PcuBo;
import com.rits.pcurouterheaderservice.model.Router;
import com.rits.pcurouterheaderservice.model.RouterList;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
