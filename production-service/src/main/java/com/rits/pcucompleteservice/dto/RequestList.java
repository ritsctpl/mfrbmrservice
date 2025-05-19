package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RequestList {
//    private List<PcuCompleteRequestInfo> requestLists;
    private List<PcuCompleteReq> requestList;
    private String accessToken;
}
