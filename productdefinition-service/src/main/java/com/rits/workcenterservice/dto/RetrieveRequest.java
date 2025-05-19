package com.rits.workcenterservice.dto;

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
    private List<String> sequence;
    private String workCenterCategory;
    private String resource;

}
