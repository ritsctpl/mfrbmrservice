package com.rits.itemgroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "R_ITEM_GROUP")
public class ItemGroup {
    @Id
    private String handle;
    private String site;
    private String itemGroup;
    private String groupDescription;
    private int unitPrice;
    private List<GroupMemberList> groupMemberList;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}