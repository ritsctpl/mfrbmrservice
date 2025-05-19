package com.rits.signoffservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignOffRequestListDetails {
    private List<SignOffRequestDetails> requestList;
    private String accessToken;
}
