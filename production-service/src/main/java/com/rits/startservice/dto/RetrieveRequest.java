package com.rits.startservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveRequest {
    private String site;
    private String workCenter;
}
