package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveComposition {
    private String siNo;
    private String sanskritName;
    private String botanicalName;
    private String scn;
    private String englishName;
    private String partUsed;
    private String bookReference;
    private String pageNum;
    private String qty;
    private String title;
    private List<ActiveCompositionData> data;
    private String tableId;
}
