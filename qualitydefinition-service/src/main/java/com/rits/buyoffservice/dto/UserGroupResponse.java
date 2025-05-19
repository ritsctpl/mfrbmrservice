package com.rits.buyoffservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGroupResponse {
    private String userGroup;
    private String description;
}
