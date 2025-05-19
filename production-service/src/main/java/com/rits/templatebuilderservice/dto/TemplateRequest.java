package com.rits.templatebuilderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    private String templateLabel;
    private String templateType;
    private String templateVersion;
    private List<GroupList> groupIds;
    private String site;
    private Boolean currentVersion;
    private String userId;
}
