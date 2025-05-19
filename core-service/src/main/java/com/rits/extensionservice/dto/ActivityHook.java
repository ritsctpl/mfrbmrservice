package com.rits.extensionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityHook {
    private int sequence;
    private String hookPoint;
    private String activity;
    private String hookableMethod;
    private Boolean enable;
    private String userArgument;

}
