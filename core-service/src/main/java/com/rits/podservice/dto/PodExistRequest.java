package com.rits.podservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PodExistRequest {
    private String site;
    private String podName;
}
