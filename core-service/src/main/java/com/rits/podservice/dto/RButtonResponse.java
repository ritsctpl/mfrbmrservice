package com.rits.podservice.dto;

import com.rits.podservice.model.Activity;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RButtonResponse {
    private String sequence;
    private String buttonType;
    private String buttonId;
    private String buttonLabel;
    private String buttonSize;
    private String imageIcon;
    private String hotKey;
    private String buttonLocation;
    private boolean startNewButtonRow;
    private List<Activity> activityList;
}
