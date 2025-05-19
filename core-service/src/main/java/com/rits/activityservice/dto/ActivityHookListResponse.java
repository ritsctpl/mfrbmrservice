package com.rits.activityservice.dto;

import com.rits.activityservice.model.ActivityHook;
import com.rits.activityservice.model.ActivityName;
import com.rits.activityservice.model.ActivityRule;
import com.rits.activityservice.model.ListOfMethods;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityHookListResponse {

    private String activityId;
    private String description;

}
