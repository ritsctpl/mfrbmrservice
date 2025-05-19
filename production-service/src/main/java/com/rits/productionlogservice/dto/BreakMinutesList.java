package com.rits.productionlogservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakMinutesList {
    private List<BreakMinutes> breakMinutesList;
}
