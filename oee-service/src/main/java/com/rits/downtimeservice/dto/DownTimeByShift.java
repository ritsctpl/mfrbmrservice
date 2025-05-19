package com.rits.downtimeservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownTimeByShift {
    private String shift;
    private Integer totalDowntime;
}
