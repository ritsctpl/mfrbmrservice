package com.rits.dccollect.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ShortRunList {
     private String dcParameterRef;

             private String  item;

             private int target;

             private int range;
}
