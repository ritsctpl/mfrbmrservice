package com.rits.nextnumbergeneratorservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponseList {
    private List<ItemResponse> itemList;

}
