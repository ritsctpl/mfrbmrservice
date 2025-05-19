package com.rits.buyoffservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGroupResponseList {
    private List<UserGroupResponse> userGroupResponses;
}
