package com.rits.podservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Activity {
    private String activitySequence;
    private String activity;
    private String type;
    private String url;
    private String pluginLocation;
    private boolean clearsPcu;
    private boolean fixed;
}
