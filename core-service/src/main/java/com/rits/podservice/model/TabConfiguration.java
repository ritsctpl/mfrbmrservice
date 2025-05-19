package com.rits.podservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TabConfiguration {
    private String activity;
    private String description;
    private List<String> buttons;
    private List<ConfigurationList> configurationList;
}
