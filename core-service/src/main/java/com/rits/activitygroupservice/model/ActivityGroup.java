package com.rits.activitygroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_ACTIVITY_GROUP")
public class ActivityGroup {
    @Id
    private String handle;
    //private String site;
    private String currentSite;
    private String activityGroupName;
    private String activityGroupDescription;
    private List<ActivityGroupMember> activityGroupMemberList;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
