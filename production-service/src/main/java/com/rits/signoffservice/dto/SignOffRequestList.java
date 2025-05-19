package com.rits.signoffservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SignOffRequestList {
    private List<SignOffRequest> requestList;
    private String accessToken;
}
