package com.rits.pcustepstatus.dto;

import com.rits.pcustepstatus.dto.PcuCompleteRequest;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RequestList {
    private List<PcuCompleteRequest> requestList;
}
