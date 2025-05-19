package com.rits.podservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DocumentRequest {
    private String site;
    private String document;
    private String version;
}
