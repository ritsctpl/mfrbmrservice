package com.rits.logbuyoffservice.model;

import com.rits.dccollect.dto.MessageDetails;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogBuyOffMessageModel {
    private MessageDetails messageDetails;
}
