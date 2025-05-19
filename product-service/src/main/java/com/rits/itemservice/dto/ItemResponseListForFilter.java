package com.rits.itemservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponseListForFilter {
    private List<ItemResponseForFilter> itemList;

}
