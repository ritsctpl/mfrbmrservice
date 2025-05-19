package com.rits.mfrservice.model;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
    private List<ActiveCompositionFields> data;
    private String tableId;
}
