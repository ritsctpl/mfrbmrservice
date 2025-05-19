package com.rits.bomservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String operation;
    private String item;
    private String revision;
    private List<ItemList> itemList;
}

