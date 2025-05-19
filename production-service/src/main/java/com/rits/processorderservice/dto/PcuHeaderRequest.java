package com.rits.processorderservice.dto;

import com.rits.pcuheaderservice.dto.Pcu;
import com.rits.pcuheaderservice.dto.PcuRequest;
import com.rits.processorderservice.model.ProcessOrder;
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
    private ProcessOrder processOrder;
    private int qtyInQueue;
    private PcuRequest pcuRequest;
    private String batchNumber;
}
