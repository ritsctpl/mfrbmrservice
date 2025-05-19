package com.rits.pcuheaderservice.model;

import com.rits.pcuheaderservice.dto.PcuRouterHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouterHeaderMessageModel {
    private PcuRouterHeader response;
    private List<MessageDetails> message_details;
}
