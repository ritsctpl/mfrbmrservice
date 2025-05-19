package com.rits.resourceservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CheckStatusRequest {
    private String site;
    private String resource;
    private String setUpState;
    private String reasonCode;
    private String comments;
    private String userId;
    private String defaultOperation;
}
