package com.rits.pcustepstatus.dto;

import com.rits.pcuheaderservice.dto.Pcu;
import com.rits.pcuheaderservice.dto.PcuRequest;
import com.rits.pcuheaderservice.dto.ShopOrder;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PcuHeaderRequest {
    private String site;
    private String pcuBO;
    private String pcuBomBO;
    private List<Pcu> pcuBos;
    private ShopOrder shopOrder;
    private String shopOrderBO;
    private int qtyInQueue;
    private PcuRequest pcuRequest;
}
