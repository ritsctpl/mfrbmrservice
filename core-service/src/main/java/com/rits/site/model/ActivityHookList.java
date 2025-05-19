package com.rits.site.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityHookList {
    private String sequence;
    private String hookPoint;
    private String activity;
    private boolean enabled;
    private String userArgument;
}
