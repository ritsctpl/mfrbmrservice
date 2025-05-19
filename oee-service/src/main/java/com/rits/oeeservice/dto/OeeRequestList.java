package com.rits.oeeservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OeeRequestList {
    private List<OeeRequest> oeeRequestList;
}
