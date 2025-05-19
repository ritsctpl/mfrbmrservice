package com.rits.customdataservice.dto;

import java.util.List;

import com.rits.customdataservice.model.CustomDataList;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomDataRequest {
    private String handle;
    private String site;
    private String category;
    private List<CustomDataList> customDataList;
    private String UserId;
}
