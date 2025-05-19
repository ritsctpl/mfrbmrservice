package com.rits.signoffservice.model;

import com.rits.signoffservice.dto.PcuInWork;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuInWorkMessageModel {
    private PcuInWork response;
    private MessageDetails message_details;
}
