package com.rits.logbuyoffservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogbuyOffRequestList {
    private List<LogbuyOffRequest> logbuyOffRequestList;
}
