package com.rits.schedulerconfigservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchedulerConfigResponse {
    private SchedulerConfig response;
    private MessageDetails message_details;
    private String errorMessage;

}
