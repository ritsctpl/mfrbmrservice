package com.rits.documentservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DocumentResponse {
    private String document;
    private String version;
    private String description;
    private boolean currentVersion;
}
