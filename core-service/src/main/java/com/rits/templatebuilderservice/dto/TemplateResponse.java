package com.rits.templatebuilderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateResponse {
    private String templateLabel;
    private String templateType;
    private String templateVersion;
    private Boolean currentVersion;
}
