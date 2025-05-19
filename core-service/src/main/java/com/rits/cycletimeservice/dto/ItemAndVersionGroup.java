package com.rits.cycletimeservice.dto;

import lombok.*;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ItemAndVersionGroup {
    private String itemAndVersion;
    private List<ItemBasedRecord> records;
}
