package com.rits.site.dto;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TimeZoneResponse {
    private List<TimeZone> timeZoneList;
}
