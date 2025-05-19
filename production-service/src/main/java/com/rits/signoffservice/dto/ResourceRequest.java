package com.rits.signoffservice.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResourceRequest {
    private String site;
    private String resource;
}
