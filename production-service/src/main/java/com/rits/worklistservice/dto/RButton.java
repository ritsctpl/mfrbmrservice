package com.rits.worklistservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RButton {
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