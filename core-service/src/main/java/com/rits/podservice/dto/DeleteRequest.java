package com.rits.podservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DeleteRequest {
    private String podName;
    private String site;
    private String userId;
}
