package com.rits.userservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AllUserResponse {
    List<AllUser> availableUsers;
}
