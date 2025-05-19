package com.rits.usergroupservice.dto;

import com.rits.usergroupservice.model.User;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserList {
    private List<User> availableUsers;
}
