package com.rits.itemgroupservice.dto;

import com.rits.itemgroupservice.model.GroupMemberList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemGroupRequest {

    private String handle;
    private String site;
    private String itemGroup;
    private String groupDescription;
    private int unitPrice;
    private List<GroupMemberList> groupMemberList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String item;
    private String userId;


}