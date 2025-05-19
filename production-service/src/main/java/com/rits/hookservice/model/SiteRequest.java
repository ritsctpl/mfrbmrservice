package com.rits.hookservice.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SiteRequest {
    private String site;
    private String handle;
    private String description;
    private String type;
    private String timeZone;
    private List<ActivityHook> activityHookLists;
//    private Theme theme;
    private boolean local;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private String userId;
}
