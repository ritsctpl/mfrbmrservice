package com.rits.worklistservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Detail {
    private List<Status> statusList;
    private String icon;
    private String status;
}
