package com.rits.cycletimeservice.model;

import com.rits.cycletimeservice.dto.CycleTimeRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CycleTimeMessageModel {
    private CycleTimeRequest response;
    private MessageDetails message_details;
}
