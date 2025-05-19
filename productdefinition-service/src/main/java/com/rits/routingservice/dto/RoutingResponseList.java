package com.rits.routingservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoutingResponseList {
    private List<RoutingResponse> routingList;
}
