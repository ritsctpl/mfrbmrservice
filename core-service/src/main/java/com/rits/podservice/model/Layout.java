package com.rits.podservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Layout {
    private String panel;
    private String type;
    private String defaultPlugin;
    private String defaultUrl;
    private List<OtherPlugin> otherPlugin;
}
