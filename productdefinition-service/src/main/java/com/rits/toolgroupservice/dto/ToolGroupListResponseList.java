package com.rits.toolgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolGroupListResponseList {
    private List<ToolGroupListResponse> toolGroupList;
}
