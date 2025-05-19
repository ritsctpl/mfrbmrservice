package com.rits.extensionservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtensionRequest {
    private String site;
    private String hookPoint;
    private String activity;
    private String hookableMethod;
    private String  request;

}
