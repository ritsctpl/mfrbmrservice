package com.rits.usergroupservice.dto;

import com.rits.usergroupservice.model.ActivityGroup;
import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SiteInfo {
    private List<String> userGroups;
    private List<ActivityGroup> permissionForActivityGroup;
}
