package com.rits.buyoffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssociateUserGroup {
    private String buyOff;
    private String version;
    private String site;
    private List<String> userGroupList;
}
