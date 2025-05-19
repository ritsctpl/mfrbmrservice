package com.rits.processorderrelease_old.dto;

import com.rits.shoporderrelease.dto.ActivityListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActivityListResponseList {
 private List<ActivityListResponse> activityList;
}
