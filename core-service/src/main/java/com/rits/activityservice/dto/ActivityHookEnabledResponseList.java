package com.rits.activityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ActivityHookEnabledResponseList {
    private List<ActivityHookEnabledResponse> enabledActivityHookList;
}
