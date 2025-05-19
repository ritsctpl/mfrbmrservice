package com.rits.scheduleservice.dto;

import com.rits.scheduleservice.model.EventScheduleConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ScheduleMessageModel {
    private EventScheduleConfig response;
    private Integer errorCode;
    private MessageDetails message_details;
}
