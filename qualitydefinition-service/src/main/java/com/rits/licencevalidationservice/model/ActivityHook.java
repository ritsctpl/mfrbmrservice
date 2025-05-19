package com.rits.licencevalidationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ActivityHook {

    private String hookPoint;
    private String activity;
    private boolean enable;
    private String userArgument;
    private String url;
}
