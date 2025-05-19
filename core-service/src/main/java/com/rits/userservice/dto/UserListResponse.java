package com.rits.userservice.dto;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserListResponse {
    private List<UserResponse> userList;
}
