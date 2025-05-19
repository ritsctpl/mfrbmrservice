package com.rits.podservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationList {
    private String activitySequence;
    private String description;
    private String activity;
    private String type;
    private String url;
    private String pluginLocation;
    private boolean clearsPcu;
    private boolean fixed;
}
