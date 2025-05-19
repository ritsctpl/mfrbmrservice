package com.rits.podservice.dto;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RButtonResponseList {
    private List<RButtonResponse> buttonList;
}
