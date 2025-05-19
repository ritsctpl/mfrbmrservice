package com.rits.userservice.dto;

import com.rits.activitygroupservice.model.ActivityGroup;
import com.rits.userservice.model.UserGroup;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {
    private String user;
    private List<String> site;
    private String currentSite;
    private String firstName;
    private String lastName;
    private String status;
    private List<UserGroup> userGroups=new ArrayList<>();
    private List<UserActivityGroupDetail> userActivityGroupDetails=new ArrayList<UserActivityGroupDetail>();
}
