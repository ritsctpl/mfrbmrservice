package com.rits.pcuheaderservice.model;

import com.rits.pcuheaderservice.dto.BomHeader;
import com.rits.pcuheaderservice.dto.PcuHeaderResponse;
import com.rits.pcuheaderservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomHeaderMessageModel {
    private BomHeader response;
    private MessageDetails message_details;
    private List<PcuHeaderResponse> pcuHeaderResponseList ;
}
