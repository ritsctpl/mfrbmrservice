package com.rits.signoffservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveRequest {
    private String site;
    private String workCenter;
}
