package com.rits.pcustepstatus.dto;

import com.rits.pcustepstatus.dto.StartRequest;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StartRequestList {
    private List<StartRequest> requestList;
}
