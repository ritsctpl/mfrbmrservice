package com.rits.lineclearancelogservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineClearanceLogRequestList {
    private List<LineClearanceLogRequest> lineClearanceLogRequestList;
}
