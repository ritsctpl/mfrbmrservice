package com.rits.itemgroupservice.dto;

import com.rits.itemgroupservice.model.ItemGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupNameResponseList {
    List<GroupNameResponse> groupNameList;


}