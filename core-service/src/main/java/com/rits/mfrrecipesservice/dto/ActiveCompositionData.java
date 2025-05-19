package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveCompositionData {
    private String siNo;
    private String sanskritName;
    private String botanicalName;
    private String scn;
    private String englishName;
    private String partUsed;
    private String bookReference;
    private String pageNum;
    private String qty;
}
