package com.rits.userservice.dto;

import com.rits.usergroupservice.model.Activity;
import com.rits.usergroupservice.model.ActivityGroup;
import com.rits.usergroupservice.model.CustomData;
import com.rits.usergroupservice.model.User;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGroupRequests {
    private String site;
    private String userGroup;
    private String description;
    private String pod;
    private List<User> users;
    private List<Activity> permissionForActivity;
    private List<ActivityGroup> permissionForActivityGroup;
    private List<CustomData> customDataList;
    private String userId;
}
