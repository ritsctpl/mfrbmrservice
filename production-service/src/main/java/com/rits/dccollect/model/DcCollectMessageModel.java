package com.rits.dccollect.model;

import com.rits.dccollect.dto.MessageDetails;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DcCollectMessageModel {
    private MessageDetails messageDetails;
}
