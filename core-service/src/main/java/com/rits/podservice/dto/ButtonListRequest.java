package com.rits.podservice.dto;

import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ButtonListRequest {
    private String podName;
    private String site;
}
