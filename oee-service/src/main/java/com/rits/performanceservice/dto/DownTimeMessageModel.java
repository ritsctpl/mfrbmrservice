package com.rits.performanceservice.dto;

import com.rits.downtimeservice.model.DownTimeAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownTimeMessageModel {
    private DownTimeAvailability response;
    private DownTimeMessageDetails message_details;
}
