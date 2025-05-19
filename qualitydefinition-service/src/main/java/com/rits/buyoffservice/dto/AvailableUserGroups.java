package com.rits.buyoffservice.dto;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AvailableUserGroups {
    private List<AvailableUserGroup> availableUserGroupList;
}
