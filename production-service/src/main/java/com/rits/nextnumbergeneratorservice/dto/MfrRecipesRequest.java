package com.rits.nextnumbergeneratorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfrRecipesRequest {
    private String site;
    private String handle;
    private String mfrNo;
    private String version;
    private String productName;
    private String modifiedBy;
    private String createdBy;
    private String type;
}
